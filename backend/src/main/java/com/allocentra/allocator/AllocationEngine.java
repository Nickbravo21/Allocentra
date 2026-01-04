package com.allocentra.allocator;

import com.allocentra.domain.*;
import com.allocentra.scoring.ScoringEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core allocation engine that distributes resources based on scores and constraints
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationEngine {

    private final ScoringEngine scoringEngine;
    private final ConstraintEngine constraintEngine;

    /**
     * Execute allocation for a cycle
     */
    public AllocationRun execute(AllocationCycle cycle, AllocationRun run) {
        log.info("Starting allocation for cycle: {}", cycle.getName());
        
        run.setStatus(AllocationRun.RunStatus.RUNNING);
        run.setStartedAt(Instant.now());
        run.setTotalRequests(cycle.getRequests().size());
        
        try {
            // Phase 1: Score all requests
            run.setCurrentPhase("Scoring requests");
            run.setProgress(0.1);
            List<ScoredRequest> scoredRequests = scoreRequests(cycle.getRequests());
            
            // Phase 2: Sort by score (highest first)
            run.setCurrentPhase("Ranking requests");
            run.setProgress(0.2);
            List<ScoredRequest> rankedRequests = rankRequests(scoredRequests);
            
            // Phase 3: Allocate greedily with constraints
            run.setCurrentPhase("Allocating resources");
            run.setProgress(0.3);
            List<AllocationResult> results = allocateResources(
                rankedRequests, 
                cycle, 
                run
            );
            
            // Phase 4: Generate explanations
            run.setCurrentPhase("Generating explanations");
            run.setProgress(0.8);
            generateExplanations(results, rankedRequests);
            
            // Phase 5: Calculate summary
            run.setCurrentPhase("Finalizing");
            run.setProgress(0.95);
            calculateSummary(run, results);
            
            run.setStatus(AllocationRun.RunStatus.COMPLETED);
            run.setCompletedAt(Instant.now());
            run.setProgress(1.0);
            run.setExecutionTimeMs(
                run.getCompletedAt().toEpochMilli() - run.getStartedAt().toEpochMilli()
            );
            
            log.info("Allocation completed: {} approved, {} partial, {} deferred, {} denied",
                run.getApprovedCount(), run.getPartialCount(), 
                run.getDeferredCount(), run.getDeniedCount());
            
        } catch (Exception e) {
            log.error("Allocation failed", e);
            run.setStatus(AllocationRun.RunStatus.FAILED);
            run.setErrorMessage(e.getMessage());
            run.setCompletedAt(Instant.now());
        }
        
        return run;
    }

    private List<ScoredRequest> scoreRequests(List<Request> requests) {
        return requests.stream()
            .map(request -> {
                double score = scoringEngine.calculateScore(request);
                request.setScore(score);
                return new ScoredRequest(request, score);
            })
            .collect(Collectors.toList());
    }

    private List<ScoredRequest> rankRequests(List<ScoredRequest> requests) {
        return requests.stream()
            .sorted(Comparator.comparingDouble(ScoredRequest::score).reversed())
            .collect(Collectors.toList());
    }

    private List<AllocationResult> allocateResources(
        List<ScoredRequest> rankedRequests,
        AllocationCycle cycle,
        AllocationRun run
    ) {
        List<AllocationResult> results = new ArrayList<>();
        Map<String, BigDecimal> budgetRemaining = initializeBudgetMap(cycle);
        Map<String, BigDecimal> resourceRemaining = initializeResourceMap(cycle);
        Map<String, AllocationResult> resultMap = new HashMap<>();
        
        int rank = 1;
        for (ScoredRequest scoredRequest : rankedRequests) {
            Request request = scoredRequest.request();
            
            AllocationResult result = AllocationResult.builder()
                .request(request)
                .status(Request.RequestStatus.PENDING)
                .amountRequested(request.getAmountRequested())
                .quantityRequested(request.getQuantityRequested())
                .score(scoredRequest.score())
                .rank(rank++)
                .constraintViolations(new ArrayList<>())
                .build();
            
            // Check dependencies first
            if (!constraintEngine.checkDependencies(request, resultMap)) {
                result.setStatus(Request.RequestStatus.DEFERRED);
                result.setReason("Dependencies not met");
                result.getConstraintViolations().add("DEPENDENCY_NOT_MET");
                results.add(result);
                resultMap.put(request.getId(), result);
                continue;
            }
            
            // Try to allocate
            if (request.getCategory() == ResourceCategory.MONEY) {
                allocateMonetary(request, result, budgetRemaining, run);
            } else {
                allocateResource(request, result, resourceRemaining, run);
            }
            
            results.add(result);
            resultMap.put(request.getId(), result);
        }
        
        return results;
    }

    private void allocateMonetary(
        Request request,
        AllocationResult result,
        Map<String, BigDecimal> budgetRemaining,
        AllocationRun run
    ) {
        String key = request.getCategory().name();
        BigDecimal remaining = budgetRemaining.getOrDefault(key, BigDecimal.ZERO);
        BigDecimal requested = request.getAmountRequested();
        
        if (remaining.compareTo(requested) >= 0) {
            // Full allocation
            result.setStatus(Request.RequestStatus.APPROVED);
            result.setAmountAllocated(requested);
            result.setReason("Fully funded");
            budgetRemaining.put(key, remaining.subtract(requested));
        } else if (run.isAllowPartialAllocations() && 
                   request.getMinimumViableAllocation() != null &&
                   remaining.compareTo(request.getMinimumViableAllocation()) >= 0) {
            // Partial allocation
            result.setStatus(Request.RequestStatus.PARTIAL);
            result.setAmountAllocated(remaining);
            result.setReason("Partially funded - budget constraint");
            result.getConstraintViolations().add("BUDGET_LIMITED");
            budgetRemaining.put(key, BigDecimal.ZERO);
        } else {
            // Denied
            result.setStatus(Request.RequestStatus.DENIED);
            result.setAmountAllocated(BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                result.setReason("Budget exhausted");
                result.getConstraintViolations().add("BUDGET_EXHAUSTED");
            } else {
                result.setReason("Below minimum viable allocation");
                result.getConstraintViolations().add("BELOW_MINIMUM_VIABLE");
            }
        }
    }

    private void allocateResource(
        Request request,
        AllocationResult result,
        Map<String, BigDecimal> resourceRemaining,
        AllocationRun run
    ) {
        String key = request.getCategory().name() + ":" + request.getResourceType();
        BigDecimal remaining = resourceRemaining.getOrDefault(key, BigDecimal.ZERO);
        BigDecimal requested = request.getQuantityRequested();
        
        if (remaining.compareTo(requested) >= 0) {
            // Full allocation
            result.setStatus(Request.RequestStatus.APPROVED);
            result.setQuantityAllocated(requested);
            result.setReason("Fully allocated");
            resourceRemaining.put(key, remaining.subtract(requested));
        } else if (run.isAllowPartialAllocations() &&
                   request.getMinimumViableQuantity() != null &&
                   remaining.compareTo(request.getMinimumViableQuantity()) >= 0) {
            // Partial allocation
            result.setStatus(Request.RequestStatus.PARTIAL);
            result.setQuantityAllocated(remaining);
            result.setReason("Partially allocated - resource constraint");
            result.getConstraintViolations().add("RESOURCE_LIMITED");
            resourceRemaining.put(key, BigDecimal.ZERO);
        } else {
            // Denied
            result.setStatus(Request.RequestStatus.DENIED);
            result.setQuantityAllocated(BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                result.setReason("Resource pool exhausted");
                result.getConstraintViolations().add("RESOURCE_EXHAUSTED");
            } else {
                result.setReason("Below minimum viable quantity");
                result.getConstraintViolations().add("BELOW_MINIMUM_VIABLE");
            }
        }
    }

    private Map<String, BigDecimal> initializeBudgetMap(AllocationCycle cycle) {
        return cycle.getBudgetPools().stream()
            .collect(Collectors.toMap(
                pool -> pool.getCategory().name(),
                BudgetPool::getTotalAmount
            ));
    }

    private Map<String, BigDecimal> initializeResourceMap(AllocationCycle cycle) {
        return cycle.getResourcePools().stream()
            .collect(Collectors.toMap(
                pool -> pool.getCategory().name() + ":" + pool.getResourceType(),
                ResourcePool::getTotalQuantity
            ));
    }

    private void generateExplanations(
        List<AllocationResult> results,
        List<ScoredRequest> rankedRequests
    ) {
        for (int i = 0; i < results.size(); i++) {
            AllocationResult result = results.get(i);
            Request request = result.getRequest();
            
            DecisionExplanation explanation = DecisionExplanation.builder()
                .result(result)
                .scoreBreakdownJson(serializeScoreBreakdown(request))
                .build();
            
            // Set reason based on status
            switch (result.getStatus()) {
                case APPROVED -> explanation.setReasonApproved(
                    "Fully funded. Ranked #" + result.getRank() + " out of " + results.size()
                );
                case PARTIAL -> explanation.setReasonPartial(
                    "Partially funded due to budget/resource constraints"
                );
                case DENIED -> explanation.setReasonDenied(
                    "Not funded. " + result.getReason()
                );
                case DEFERRED -> explanation.setReasonDeferred(
                    "Deferred. " + result.getReason()
                );
            }
            
            // Compare to next/previous request
            if (i < results.size() - 1) {
                AllocationResult nextResult = results.get(i + 1);
                explanation.setComparedToRequestId(nextResult.getRequest().getId());
                explanation.setComparedToRequestTitle(nextResult.getRequest().getTitle());
                explanation.setComparedToScore(nextResult.getScore());
                explanation.setScoreDifference(result.getScore() - nextResult.getScore());
            }
            
            result.setExplanation(explanation);
        }
    }

    private String serializeScoreBreakdown(Request request) {
        ScoringEngine.ScoreBreakdown breakdown = scoringEngine.calculateBreakdown(request);
        // In production, use Jackson to serialize properly
        return String.format(
            "{\"totalScore\":%.2f,\"priority\":{\"value\":%.2f,\"contribution\":%.2f},\"urgency\":{\"value\":%.2f,\"contribution\":%.2f},\"impact\":{\"value\":%.2f,\"contribution\":%.2f},\"risk\":{\"value\":%.2f,\"contribution\":%.2f},\"strategic\":{\"value\":%.2f,\"contribution\":%.2f}}",
            breakdown.getTotalScore(),
            breakdown.getPriority().getValue(), breakdown.getPriority().getContribution(),
            breakdown.getUrgency().getValue(), breakdown.getUrgency().getContribution(),
            breakdown.getImpact().getValue(), breakdown.getImpact().getContribution(),
            breakdown.getRisk().getValue(), breakdown.getRisk().getContribution(),
            breakdown.getStrategic().getValue(), breakdown.getStrategic().getContribution()
        );
    }

    private void calculateSummary(AllocationRun run, List<AllocationResult> results) {
        long approved = results.stream()
            .filter(r -> r.getStatus() == Request.RequestStatus.APPROVED)
            .count();
        long partial = results.stream()
            .filter(r -> r.getStatus() == Request.RequestStatus.PARTIAL)
            .count();
        long deferred = results.stream()
            .filter(r -> r.getStatus() == Request.RequestStatus.DEFERRED)
            .count();
        long denied = results.stream()
            .filter(r -> r.getStatus() == Request.RequestStatus.DENIED)
            .count();
        
        BigDecimal totalAllocated = results.stream()
            .map(r -> r.getAmountAllocated() != null ? r.getAmountAllocated() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        run.setApprovedCount((int) approved);
        run.setPartialCount((int) partial);
        run.setDeferredCount((int) deferred);
        run.setDeniedCount((int) denied);
        run.setTotalAllocated(totalAllocated);
        
        // Add results to run
        results.forEach(run::addResult);
    }

    private record ScoredRequest(Request request, double score) {}
}
