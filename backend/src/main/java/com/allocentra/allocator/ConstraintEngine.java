package com.allocentra.allocator;

import com.allocentra.domain.AllocationResult;
import com.allocentra.domain.Request;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Validates and enforces allocation constraints
 */
@Service
public class ConstraintEngine {

    /**
     * Check if all dependencies of a request are satisfied
     */
    public boolean checkDependencies(
        Request request,
        Map<String, AllocationResult> resultMap
    ) {
        if (request.getDependencies() == null || request.getDependencies().isEmpty()) {
            return true;
        }
        
        for (String dependencyId : request.getDependencies()) {
            AllocationResult depResult = resultMap.get(dependencyId);
            
            // Dependency not yet processed or not approved
            if (depResult == null || depResult.getStatus() != Request.RequestStatus.APPROVED) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Validate cycle constraints before allocation starts
     */
    public void validateCycle(com.allocentra.domain.AllocationCycle cycle) {
        // Check for circular dependencies
        // Check for requests exceeding total budget
        // Check for missing resource pools
        // This would be implemented with graph traversal for dependencies
    }
}
