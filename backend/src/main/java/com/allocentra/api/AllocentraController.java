package com.allocentra.api;

import com.allocentra.domain.*;
import com.allocentra.repository.*;
import com.allocentra.allocator.AllocationEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Main REST API for Allocentra
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Allocentra API", description = "Resource allocation operations")
public class AllocentraController {

    private final AllocationCycleRepository cycleRepository;
    private final RequestRepository requestRepository;
    private final AllocationRunRepository runRepository;
    private final AllocationEngine allocationEngine;
    
    @Value("${allocentra.engine.version}")
    private String engineVersion;

    // ============ CYCLES ============

    @PostMapping("/cycles")
    @Operation(summary = "Create allocation cycle")
    public ResponseEntity<AllocationCycle> createCycle(@RequestBody AllocationCycle cycle) {
        AllocationCycle saved = cycleRepository.save(cycle);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/cycles/{id}")
    @Operation(summary = "Get cycle details")
    public ResponseEntity<AllocationCycle> getCycle(@PathVariable String id) {
        return cycleRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cycles")
    @Operation(summary = "List all cycles")
    public ResponseEntity<List<AllocationCycle>> listCycles(
        @RequestParam(required = false) AllocationCycle.CycleStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(cycleRepository.findByStatus(status));
        }
        return ResponseEntity.ok(cycleRepository.findAll());
    }

    // ============ REQUESTS ============

    @PostMapping("/requests")
    @Operation(summary = "Create request")
    public ResponseEntity<Request> createRequest(@RequestBody Request request) {
        Request saved = requestRepository.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/requests")
    @Operation(summary = "List requests")
    public ResponseEntity<Page<Request>> listRequests(
        @RequestParam String cycleId,
        @RequestParam(required = false) Request.RequestStatus status,
        @RequestParam(required = false) ResourceCategory category,
        Pageable pageable
    ) {
        Page<Request> requests;
        
        if (status != null) {
            requests = requestRepository.findByCycleIdAndStatus(cycleId, status, pageable);
        } else if (category != null) {
            requests = requestRepository.findByCycleIdAndCategory(cycleId, category, pageable);
        } else {
            requests = requestRepository.findByCycleId(cycleId, pageable);
        }
        
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/{id}")
    @Operation(summary = "Get request details")
    public ResponseEntity<Request> getRequest(@PathVariable String id) {
        return requestRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ============ ALLOCATION RUNS ============

    @PostMapping("/runs")
    @Operation(summary = "Execute allocation")
    public ResponseEntity<Map<String, Object>> runAllocation(
        @RequestBody Map<String, Object> runRequest
    ) {
        String cycleId = (String) runRequest.get("cycleId");
        AllocationCycle cycle = cycleRepository.findById(cycleId)
            .orElseThrow(() -> new RuntimeException("Cycle not found"));
        
        AllocationRun run = AllocationRun.builder()
            .cycle(cycle)
            .engineVersion(engineVersion)
            .allowPartialAllocations((Boolean) runRequest.getOrDefault("allowPartialAllocations", true))
            .notes((String) runRequest.get("notes"))
            .build();
        
        AllocationRun savedRun = runRepository.save(run);
        
        // Execute asynchronously
        executeAllocationAsync(savedRun.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("runId", savedRun.getId());
        response.put("status", "RUNNING");
        response.put("message", "Allocation engine started. Poll /runs/" + savedRun.getId() + " for results.");
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Async
    public void executeAllocationAsync(String runId) {
        AllocationRun run = runRepository.findById(runId)
            .orElseThrow(() -> new RuntimeException("Run not found"));
        AllocationCycle cycle = cycleRepository.findByIdWithPools(run.getCycle().getId());
        run.setCycle(cycle);
        
        AllocationRun completed = allocationEngine.execute(cycle, run);
        runRepository.save(completed);
    }

    @GetMapping("/runs/{id}")
    @Operation(summary = "Get run status and results")
    public ResponseEntity<Map<String, Object>> getRunStatus(@PathVariable String id) {
        AllocationRun run = runRepository.findByIdWithResults(id);
        if (run == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("runId", run.getId());
        response.put("status", run.getStatus());
        response.put("cycleId", run.getCycle().getId());
        
        if (run.getStatus() == AllocationRun.RunStatus.RUNNING) {
            response.put("progress", run.getProgress());
            response.put("currentPhase", run.getCurrentPhase());
        } else if (run.getStatus() == AllocationRun.RunStatus.COMPLETED) {
            response.put("completedAt", run.getCompletedAt());
            response.put("executionTimeMs", run.getExecutionTimeMs());
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRequests", run.getTotalRequests());
            summary.put("approved", run.getApprovedCount());
            summary.put("partial", run.getPartialCount());
            summary.put("deferred", run.getDeferredCount());
            summary.put("denied", run.getDeniedCount());
            summary.put("totalAllocated", run.getTotalAllocated());
            summary.put("budgetUtilization", run.getBudgetUtilization());
            response.put("summary", summary);
            
            response.put("results", run.getResults());
        } else if (run.getStatus() == AllocationRun.RunStatus.FAILED) {
            response.put("errorMessage", run.getErrorMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/runs")
    @Operation(summary = "List allocation runs")
    public ResponseEntity<List<AllocationRun>> listRuns(
        @RequestParam(required = false) String cycleId,
        @RequestParam(required = false) AllocationRun.RunStatus status
    ) {
        if (cycleId != null) {
            return ResponseEntity.ok(runRepository.findByCycleIdOrderByCreatedAtDesc(cycleId));
        } else if (status != null) {
            return ResponseEntity.ok(runRepository.findByStatusOrderByCreatedAtDesc(status));
        }
        return ResponseEntity.ok(runRepository.findAll());
    }

    // ============ HEALTH CHECK ============

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("version", engineVersion);
        health.put("service", "Allocentra Backend");
        return ResponseEntity.ok(health);
    }
}
