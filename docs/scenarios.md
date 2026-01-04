# Allocentra Scenario Engine

## Overview

The scenario engine allows "what-if" analysis by modifying allocation parameters and comparing outcomes. This is critical for planning and risk assessment.

## Core Concept

1. **Baseline**: Run allocation with real data
2. **Scenario**: Create modified version with parameter changes
3. **Run**: Execute allocation with modified parameters
4. **Compare**: Show deltas between baseline and scenario

---

## Scenario Types

### 1. Budget Adjustment

**Use Case**: Budget cut or increase

**Parameters**:
```json
{
  "type": "BUDGET_ADJUSTMENT",
  "budgetMultiplier": 0.80,  // 20% cut
  "categorySpecific": {
    "TRAINING": 0.50,  // 50% cut to training specifically
    "EQUIPMENT": 1.0   // No change to equipment
  }
}
```

**Example**:
```
Baseline Budget: $500,000
Scenario Budget: $400,000 (20% cut)

Baseline: 23 approved, 8 partial, 11 deferred, 5 denied
Scenario: 18 approved, 6 partial, 15 deferred, 8 denied

Impact: 5 fewer approvals, 4 more deferrals
```

---

### 2. Resource Loss

**Use Case**: Vehicle down, personnel shortage

**Parameters**:
```json
{
  "type": "RESOURCE_LOSS",
  "resourceChanges": [
    {
      "resourceType": "TRUCK",
      "quantityChange": -2
    },
    {
      "resourceType": "PERSONNEL",
      "hoursChange": -500
    }
  ]
}
```

**Example**:
```
Baseline: 5 trucks available
Scenario: 3 trucks available (2 out for maintenance)

Impacted Requests:
- "Field Response Team Deployment" → DEFERRED
- "Equipment Transport" → PARTIAL (reduced scope)
```

---

### 3. Surge Event

**Use Case**: Emergency increases demand

**Parameters**:
```json
{
  "type": "SURGE_EVENT",
  "injectRequests": [
    {
      "title": "Emergency Response - Hurricane Prep",
      "category": "MONEY",
      "amountRequested": 75000.00,
      "priority": 5,
      "urgencyDeadline": "2026-01-10",
      "impact": "CRITICAL",
      "risk": "SAFETY"
    }
  ],
  "budgetIncrease": 50000.00  // Emergency fund release
}
```

**Example**:
```
Baseline: 47 requests
Scenario: 48 requests (1 emergency injection)

Emergency request ranks #1 with score 4.95
Displaces 2 previously approved requests → now DEFERRED
```

---

### 4. Priority Re-Weighting

**Use Case**: Change organizational priorities

**Parameters**:
```json
{
  "type": "WEIGHT_ADJUSTMENT",
  "scoreWeights": {
    "priority": 0.20,
    "urgency": 0.15,
    "impact": 0.30,
    "risk": 0.30,     // Emphasize risk more
    "strategic": 0.05
  }
}
```

**Example**:
```
Baseline Weights: Priority=30%, Risk=15%
Scenario Weights: Priority=20%, Risk=30%

Result: Safety-related requests move up in ranking
- "Vehicle Safety Inspection" → Approved (was Partial)
- "Office Furniture" → Denied (was Approved)
```

---

### 5. Category Cap Adjustment

**Use Case**: Policy change limits spending

**Parameters**:
```json
{
  "type": "CATEGORY_CAP_ADJUSTMENT",
  "categoryCaps": {
    "TRAINING": 0.15,  // Reduce from 25% to 15%
    "TRAVEL": 0.05     // Reduce from 10% to 5%
  }
}
```

**Example**:
```
Baseline: Training requests totaling $125,000 approved (25% cap)
Scenario: Training limited to $75,000 (15% cap)

Impacted: 3 training requests moved from APPROVED to PARTIAL
Total Training Impact: -$50,000
```

---

### 6. Request Removal

**Use Case**: Request withdrawn or cancelled

**Parameters**:
```json
{
  "type": "REQUEST_REMOVAL",
  "removeRequests": ["01JGAAA...", "01JGBBB..."]
}
```

**Example**:
```
Baseline: 47 requests, budget 64% utilized
Scenario: 45 requests (2 withdrawn)

Result: $30,000 budget freed
- 2 previously DEFERRED requests now APPROVED
```

---

### 7. Dependency Injection

**Use Case**: Add new dependency requirement

**Parameters**:
```json
{
  "type": "DEPENDENCY_INJECTION",
  "requestId": "01JGCCC...",
  "newDependencies": ["01JGDDD..."]
}
```

**Example**:
```
Request "Training Program" previously independent
Scenario: Add dependency on "Facility Upgrade"

If "Facility Upgrade" is DENIED → "Training Program" becomes DEFERRED
```

---

## Scenario Execution

### Flow

```
1. Load baseline cycle data
2. Apply scenario modifications
3. Create temporary modified cycle
4. Run allocation engine
5. Store scenario results
6. Generate comparison report
```

### API

```http
POST /api/scenarios
{
  "name": "Budget Cut 20%",
  "baseCycleId": "01JGXXX...",
  "modifications": { ... }
}

Response: 201 Created
{
  "scenarioId": "01JGCCC...",
  "status": "CREATED"
}
```

```http
POST /api/scenarios/{scenarioId}/run

Response: 202 Accepted
{
  "scenarioRunId": "01JGDDD...",
  "status": "RUNNING"
}
```

```http
GET /api/scenarios/{scenarioId}/results

Response: 200 OK
{
  "scenarioId": "01JGCCC...",
  "baseline": { ... },
  "scenario": { ... },
  "deltas": { ... },
  "impactedRequests": [ ... ]
}
```

---

## Comparison Output

### Summary Comparison

```json
{
  "baseline": {
    "totalRequests": 47,
    "approved": 23,
    "partial": 8,
    "deferred": 11,
    "denied": 5,
    "totalAllocated": 320000.00,
    "budgetUtilization": 0.64
  },
  "scenario": {
    "totalRequests": 47,
    "approved": 18,
    "partial": 6,
    "deferred": 15,
    "denied": 8,
    "totalAllocated": 256000.00,
    "budgetUtilization": 0.64
  },
  "deltas": {
    "approvedDelta": -5,
    "partialDelta": -2,
    "deferredDelta": +4,
    "deniedDelta": +3,
    "allocationDelta": -64000.00,
    "utilizationDelta": 0.00
  }
}
```

### Request-Level Changes

```json
{
  "impactedRequests": [
    {
      "requestId": "01JGEEE...",
      "title": "Training Program Extension",
      "baselineStatus": "APPROVED",
      "baselineAmount": 12000.00,
      "scenarioStatus": "PARTIAL",
      "scenarioAmount": 8000.00,
      "delta": -4000.00,
      "reason": "Category cap reduced training budget from 25% to 15%",
      "rank": {
        "baseline": 12,
        "scenario": 12,
        "change": 0
      }
    },
    {
      "requestId": "01JGFFF...",
      "title": "Office Furniture Upgrade",
      "baselineStatus": "DEFERRED",
      "baselineAmount": 0.00,
      "scenarioStatus": "DENIED",
      "scenarioAmount": 0.00,
      "delta": 0.00,
      "reason": "Lower budget exhausted earlier in allocation",
      "rank": {
        "baseline": 43,
        "scenario": 43,
        "change": 0
      }
    }
  ]
}
```

### Category Impact

```json
{
  "categoryImpact": [
    {
      "category": "TRAINING",
      "baseline": {
        "budget": 125000.00,
        "allocated": 125000.00,
        "approvedCount": 8
      },
      "scenario": {
        "budget": 75000.00,
        "allocated": 75000.00,
        "approvedCount": 5
      },
      "delta": {
        "budgetChange": -50000.00,
        "allocationChange": -50000.00,
        "countChange": -3
      }
    }
  ]
}
```

---

## Visualization

### Sankey Diagram

Shows request flow from baseline to scenario:

```
Baseline                Scenario
APPROVED (23) ────────► APPROVED (18)
              \       ┌► PARTIAL (6)
               └──────┤
PARTIAL (8) ──────────► DEFERRED (15)
DEFERRED (11) ────────► DENIED (8)
DENIED (5)
```

### Waterfall Chart

Shows budget allocation changes:

```
Baseline: $320,000
  - Budget Cut: -$64,000
  - Category Caps: -$15,000
  + Request Removals: +$5,000
Scenario: $256,000
```

---

## Multiple Scenario Comparison

Compare 3+ scenarios side-by-side:

```json
{
  "scenarioNames": ["Baseline", "Cut 10%", "Cut 20%", "Cut 30%"],
  "metrics": [
    {
      "metric": "Approved Requests",
      "values": [23, 21, 18, 15]
    },
    {
      "metric": "Budget Allocated",
      "values": [320000, 288000, 256000, 224000]
    },
    {
      "metric": "High Priority Denied",
      "values": [0, 1, 3, 6]
    }
  ]
}
```

---

## Sensitivity Analysis

**Question**: How sensitive are results to budget changes?

```json
{
  "sensitivityAnalysis": {
    "parameter": "budgetMultiplier",
    "range": [0.70, 0.75, 0.80, 0.85, 0.90, 0.95, 1.00],
    "results": [
      { "multiplier": 0.70, "approved": 13, "denied": 18 },
      { "multiplier": 0.75, "approved": 15, "denied": 15 },
      { "multiplier": 0.80, "approved": 18, "denied": 10 },
      { "multiplier": 0.85, "approved": 20, "denied": 8 },
      { "multiplier": 0.90, "approved": 22, "denied": 6 },
      { "multiplier": 0.95, "approved": 23, "denied": 5 },
      { "multiplier": 1.00, "approved": 23, "denied": 5 }
      ],
    "insights": {
      "breakpoints": [
        {
          "multiplier": 0.85,
          "description": "Below 85% budget, high-priority requests start getting denied"
        }
      ]
    }
  }
}
```

---

## Scenario Caching

### Performance Optimization

Scenarios with identical modifications can be cached:

```java
String cacheKey = hashScenarioConfig(scenario);
if (cache.contains(cacheKey)) {
    return cache.get(cacheKey);
}
```

### Cache Key

```
Hash(baseCycleId + modifications + engineVersion)
```

### Invalidation

Cache invalidated when:
- Base cycle data changes
- Engine version updates
- Explicit user request

---

## Testing Scenarios

### Unit Tests

```java
@Test
void testBudgetCutScenario() {
    Cycle baseline = createCycle();
    AllocationResult baselineResult = engine.run(baseline);
    
    Scenario scenario = Scenario.builder()
        .baseCycle(baseline)
        .budgetMultiplier(0.80)
        .build();
    
    AllocationResult scenarioResult = engine.runScenario(scenario);
    
    assertThat(scenarioResult.getApprovedCount())
        .isLessThan(baselineResult.getApprovedCount());
}
```

---

## Use Cases

### Strategic Planning

**Scenario**: "What if we lose 20% of budget mid-year?"
**Action**: Identify which programs are at risk, plan contingencies

### Risk Assessment

**Scenario**: "What if we have 3 emergency requests next month?"
**Action**: Ensure critical operations can still be funded

### Policy Impact

**Scenario**: "What if we cap training at 15% instead of 25%?"
**Action**: Understand training program impact before policy change

### Resource Planning

**Scenario**: "What if 2 vehicles are out for maintenance?"
**Action**: Identify dependent operations, plan backups

---

## Future Enhancements

### Monte Carlo Simulation

Run 1000+ scenarios with randomized parameters:
- Budget varies ±10%
- 1-3 emergency requests injected
- Random resource outages

Output: Probability distribution of outcomes

### Optimization Mode

**Goal**: Find optimal scenario that maximizes approvals within constraints

```
Maximize: Approved Requests
Subject to: Budget ≤ $500,000
            Category Caps Respected
            All Critical Requests Approved
```

### Scenario Templates

Pre-built scenarios:
- "Standard Budget Cut (10%/20%/30%)"
- "Emergency Surge Event"
- "Resource Shortage"
- "Policy Change"

### Collaborative Scenarios

Multiple users can create and share scenarios:
- Tag scenarios: "FY2026 Planning", "Risk Assessment"
- Comment on scenario outcomes
- Vote on most realistic scenarios

---

## Performance Considerations

### Execution Time

- **Small Cycle** (<100 requests): <500ms
- **Medium Cycle** (100-500 requests): <2s
- **Large Cycle** (500-1000 requests): <5s

### Parallel Execution

Multiple scenarios can run in parallel:
- Each scenario in separate thread
- Shared baseline data (read-only)
- Independent result storage

### Resource Usage

- **Memory**: ~50MB per scenario run
- **CPU**: 1 core per scenario
- **Database**: 1 transaction per scenario

---

## Audit Trail

Every scenario run is logged:

```json
{
  "scenarioId": "01JGCCC...",
  "runId": "01JGDDD...",
  "userId": "user@example.com",
  "timestamp": "2026-01-03T11:00:00Z",
  "modifications": { ... },
  "results": { ... },
  "executionTimeMs": 1842
}
```

This ensures all "what-if" analysis is documented and repeatable.
