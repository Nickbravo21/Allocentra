# Allocentra Constraints Engine

## Overview

Constraints prevent invalid or suboptimal allocations. The allocation engine sorts requests by score, then allocates greedily while enforcing all constraints.

## Constraint Types

### 1. Budget Constraints

**Rule**: Cannot allocate more than available budget per category.

**Implementation**:
```java
if (remainingBudget < request.getAmountRequested()) {
    if (allowPartialAllocations && remainingBudget >= request.getMinimumViableAllocation()) {
        allocate(remainingBudget); // Partial
    } else {
        deny("BUDGET_EXHAUSTED");
    }
}
```

**Example**:
```
Total Budget: $500,000
Allocated: $485,000
Remaining: $15,000

Request: $20,000
Minimum Viable: $12,000
Result: PARTIAL allocation of $15,000
```

**Explanation Generated**:
> "Budget constraint limited allocation. $15,000 allocated from $20,000 requested. This was the maximum available in the MONEY category."

---

### 2. Resource Pool Constraints

**Rule**: Cannot allocate more resources than available in pool.

**Implementation**:
```java
ResourcePool pool = getPool(request.getCategory(), request.getResourceType());
if (pool.getRemainingQuantity() < request.getQuantityRequested()) {
    if (pool.getRemainingQuantity() >= request.getMinimumViableQuantity()) {
        allocate(pool.getRemainingQuantity()); // Partial
    } else {
        deny("RESOURCE_EXHAUSTED");
    }
}
```

**Example**:
```
Vehicle Pool: 5 trucks
Allocated: 4 trucks
Remaining: 1 truck

Request: 2 trucks
Minimum Viable: 1 truck
Result: PARTIAL allocation of 1 truck
```

**Explanation Generated**:
> "Resource pool constraint: Only 1 truck remaining. Allocated 1 of 2 requested. Request marked as PARTIAL."

---

### 3. Dependency Constraints

**Rule**: Cannot approve a request if its dependencies are not approved.

**Implementation**:
```java
for (String dependencyId : request.getDependencies()) {
    AllocationResult depResult = getResult(dependencyId);
    if (depResult.getStatus() != Status.APPROVED) {
        defer("DEPENDENCY_NOT_MET: " + dependencyId);
        return;
    }
}
```

**Example**:
```
Request A: "Hire Personnel" → DENIED (budget exhausted)
Request B: "Training for New Personnel" → depends on A
Result: Request B is DEFERRED
```

**Explanation Generated**:
> "Cannot approve: dependency 'Hire Personnel' (ID: 01JG...) was not approved. Status: DENIED. This request will be reconsidered if the dependency is resolved."

**Dependency Chain Handling**:
- Dependencies must be acyclic (validated at request creation)
- Transitive dependencies are resolved automatically
- Circular dependencies result in validation error

---

### 4. Category Cap Constraints

**Rule**: No category can exceed a specified percentage of total budget.

**Configuration**:
```json
{
  "categoryCaps": {
    "TRAINING": 0.25,
    "EQUIPMENT": 0.40,
    "TRAVEL": 0.10
  }
}
```

**Implementation**:
```java
double categoryTotal = getCategoryTotal(request.getCategory());
double cap = totalBudget * categoryCap;

if (categoryTotal + request.getAmountRequested() > cap) {
    double available = cap - categoryTotal;
    if (available >= request.getMinimumViableAllocation()) {
        allocate(available); // Partial
    } else {
        deny("CATEGORY_CAP_EXCEEDED");
    }
}
```

**Example**:
```
Total Budget: $500,000
Training Cap: 25% = $125,000
Training Allocated: $120,000
Training Remaining: $5,000

Request: "Advanced Training Program" - $15,000
Result: PARTIAL allocation of $5,000 (if minimum viable is ≤ $5,000)
        or DENIED (if minimum viable > $5,000)
```

**Explanation Generated**:
> "Category cap constraint: TRAINING limited to 25% of budget ($125,000). Already allocated $120,000. Only $5,000 available. Request requires $15,000."

---

### 5. Minimum Viable Allocation Constraints

**Rule**: If partial allocation is below minimum viable, deny the request.

**Implementation**:
```java
if (availableAmount < request.getAmountRequested()) {
    if (availableAmount >= request.getMinimumViableAllocation()) {
        allocate(availableAmount); // Partial OK
    } else {
        deny("BELOW_MINIMUM_VIABLE");
    }
}
```

**Example**:
```
Request: $50,000
Minimum Viable: $30,000
Available: $20,000
Result: DENIED (below minimum)

Request: $50,000
Minimum Viable: $30,000
Available: $35,000
Result: PARTIAL ($35,000 allocated)
```

**Explanation Generated**:
> "Only $20,000 available, but request requires minimum $30,000 to be viable. Denied to avoid underfunding."

---

### 6. Resource Exclusivity Constraints

**Rule**: Some resources cannot be double-allocated.

**Example**: Same vehicle cannot be allocated to two concurrent requests.

**Implementation**:
```java
if (resource.isExclusive() && resource.isAllocated(timeWindow)) {
    defer("RESOURCE_CONFLICT");
}
```

**Use Case**:
```
Request A: Vehicle #1 for Jan 1-15 → APPROVED
Request B: Vehicle #1 for Jan 10-20 → DEFERRED (conflict)
```

**Explanation Generated**:
> "Resource exclusivity constraint: Vehicle #1 already allocated to 'Emergency Response Training' for overlapping period (Jan 1-15)."

---

### 7. Temporal Constraints

**Rule**: Cannot allocate resources outside the cycle time window.

**Implementation**:
```java
if (request.getStartDate().isBefore(cycle.getStartDate()) ||
    request.getEndDate().isAfter(cycle.getEndDate())) {
    deny("OUT_OF_CYCLE_WINDOW");
}
```

**Example**:
```
Cycle: Q1 2026 (Jan 1 - Mar 31)
Request needs resources: Feb 15 - Apr 10
Result: DENIED (extends beyond cycle)
```

**Explanation Generated**:
> "Request extends beyond cycle end date (Mar 31, 2026). End date Apr 10, 2026 is out of bounds."

---

### 8. Pre-Check Warnings (Non-Blocking)

**Purpose**: Warn about potential issues before allocation run.

**Checks**:
- Impossible dependencies (Request A depends on B, B depends on A)
- Requests exceeding total budget (even if highest priority)
- Missing resource pools for requested categories
- Zero available budget in category

**Implementation**:
```java
List<String> warnings = preCheckEngine.validate(cycle);
// Returns warnings but does not block allocation
```

**Example Output**:
```json
{
  "warnings": [
    {
      "type": "EXCEEDS_TOTAL_BUDGET",
      "requestId": "01JG...",
      "message": "Request for $600,000 exceeds total cycle budget of $500,000"
    },
    {
      "type": "MISSING_RESOURCE_POOL",
      "requestId": "01JH...",
      "message": "No resource pool defined for category DRONES"
    }
  ]
}
```

---

## Constraint Evaluation Order

**Order matters for performance and correctness:**

1. **Pre-Validation** (before allocation starts)
   - Cycle window check
   - Resource pool existence
   - Dependency cycles

2. **Per-Request During Allocation** (sorted by score, highest first)
   - Dependency check
   - Budget availability
   - Resource pool availability
   - Category cap check
   - Minimum viable check
   - Exclusivity check

3. **Post-Allocation Validation**
   - Total budget not exceeded
   - Category caps respected
   - All approved requests satisfy dependencies

---

## Constraint Handling Strategies

### Strategy 1: Strict (Default)

- All constraints are hard limits
- No constraint can be violated
- Results in more denials/deferrals

### Strategy 2: Soft (Future Extension)

- Some constraints can be "overridden" with justification
- Requires manual approval
- Logged as exceptions

### Strategy 3: Optimization (Future Extension)

- Use constraint relaxation to maximize allocations
- Example: Slightly exceed category cap if it allows funding critical request
- Would require configurable "flex" percentages

---

## Constraint Priority

When multiple constraints conflict, the engine uses this priority:

1. **Dependencies** (highest) - prevents broken chains
2. **Safety/Legal Requirements** - prevent risk
3. **Category Caps** - organizational policy
4. **Budget/Resource Limits** - hard resource constraints
5. **Minimum Viable** - quality control

**Example**:

```
Request has:
- Unmet dependency → DEFERRED (even if budget available)
- Met dependency but budget exhausted → DENIED

Priority: Dependency > Budget
```

---

## Constraint Violation Explanations

Each constraint violation generates a structured explanation:

```json
{
  "constraintType": "BUDGET_EXHAUSTED",
  "severity": "BLOCKING",
  "message": "Budget fully allocated to higher-priority requests",
  "details": {
    "categoryBudget": 500000.00,
    "allocated": 485000.00,
    "remaining": 15000.00,
    "requested": 20000.00,
    "minimumViable": 12000.00
  },
  "remediation": [
    "Request in next allocation cycle",
    "Reduce requested amount to $15,000 or less",
    "Increase cycle budget",
    "Request partial allocation if $15,000 is sufficient"
  ]
}
```

---

## Testing Constraints

### Unit Tests

```java
@Test
void testBudgetConstraint_Denial() {
    BudgetPool pool = BudgetPool.builder()
        .totalAmount(100000.00)
        .allocated(95000.00)
        .build();
    
    Request request = Request.builder()
        .amountRequested(10000.00)
        .minimumViableAllocation(8000.00)
        .build();
    
    AllocationResult result = allocator.allocate(request, pool);
    
    assertThat(result.getStatus()).isEqualTo(Status.DENIED);
    assertThat(result.getConstraintViolations())
        .contains("BUDGET_EXHAUSTED");
}

@Test
void testDependencyConstraint() {
    Request dependency = createRequest("Dependency");
    Request dependent = createRequest("Dependent")
        .dependencies(List.of(dependency.getId()));
    
    // Deny dependency
    allocationEngine.deny(dependency);
    
    // Try to allocate dependent
    AllocationResult result = allocationEngine.allocate(dependent);
    
    assertThat(result.getStatus()).isEqualTo(Status.DEFERRED);
    assertThat(result.getReason()).contains("DEPENDENCY_NOT_MET");
}
```

---

## Constraint Configuration

### Per-Cycle Configuration

```json
{
  "cycleId": "01JGXXX...",
  "constraintConfig": {
    "allowPartialAllocations": true,
    "categoryCaps": {
      "TRAINING": 0.25,
      "TRAVEL": 0.10
    },
    "enforceDependencies": true,
    "enforceMinimumViable": true,
    "resourceExclusivity": true,
    "strictMode": false
  }
}
```

### System-Wide Defaults

```yaml
# application.yml
allocentra:
  constraints:
    default-allow-partial: true
    default-category-caps:
      TRAINING: 0.25
      TRAVEL: 0.10
      EQUIPMENT: 0.40
    enforce-dependencies: true
    enforce-minimum-viable: true
    max-requests-per-cycle: 1000
```

---

## Performance Considerations

### Complexity

- **Dependency Check**: O(d) where d = number of dependencies per request
- **Budget Check**: O(1)
- **Category Cap**: O(1) with cached category totals
- **Overall**: O(n) for n requests (after sorting)

### Optimization

- Cache category totals during allocation pass
- Pre-compute dependency graph
- Use indexed lookups for resource pools
- Batch database queries

---

## Future Constraint Types

### Planned Enhancements

1. **Geo-Location Constraints**
   - Resources available only in certain regions
   - Distance/travel time limits

2. **Skill Requirements**
   - Personnel must have required certifications
   - Equipment requires trained operators

3. **Seasonal Constraints**
   - Some resources unavailable during certain periods
   - Weather-dependent operations

4. **Mutual Exclusivity**
   - Approve either Request A or B, not both
   - Choice groups

5. **Quota Constraints**
   - Max N requests per department
   - Fairness constraints

6. **Multi-Cycle Constraints**
   - Total allocation across multiple cycles
   - Annual caps
