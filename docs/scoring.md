# Allocentra Scoring Engine

## Overview

The scoring engine is the heart of Allocentra. It takes multi-dimensional request attributes and produces a single, comparable score that reflects the request's overall importance and urgency.

## Scoring Formula

```
Score = (Priority × 0.30) + (Urgency × 0.25) + (Impact × 0.25) + (Risk × 0.15) + (Strategic × 0.05)
```

### Score Range

- **Minimum**: 0.0
- **Maximum**: 5.0
- **Typical Range**: 1.5 - 4.5

---

## Scoring Components

### 1. Priority (30% weight)

**Input**: Manual priority level set by requester or approver

**Scale**: 1 to 5

| Priority | Description | Example |
|----------|-------------|---------|
| 1 | Nice to have | Office comfort improvements |
| 2 | Low priority | Non-urgent upgrades |
| 3 | Standard | Regular operational needs |
| 4 | High priority | Important but not urgent |
| 5 | Critical | Mission-critical, safety, legal |

**Normalization**: Already on 1-5 scale, used directly.

**Contribution Example**:
- Priority 5 → 5 × 0.30 = **1.50**
- Priority 3 → 3 × 0.30 = **0.90**
- Priority 1 → 1 × 0.30 = **0.30**

---

### 2. Urgency (25% weight)

**Input**: Days until deadline

**Calculation**:
```
Urgency Score = 5 - min(4, (days_until_deadline / 30))
```

**Logic**:
- 0-7 days → Score ≈ 5.0 (immediate)
- 30 days → Score = 4.0
- 60 days → Score = 3.0
- 90 days → Score = 2.0
- 120+ days → Score = 1.0

**Normalization**: Inverse relationship - closer deadline = higher score.

**Contribution Example**:
- 7 days → 4.77 × 0.25 = **1.19**
- 30 days → 4.0 × 0.25 = **1.00**
- 90 days → 2.0 × 0.25 = **0.50**

---

### 3. Impact (25% weight)

**Input**: Categorical impact level

**Scale**:

| Impact | Score | Description | Example |
|--------|-------|-------------|---------|
| LOW | 1 | Minimal operational impact | Aesthetic improvements |
| MEDIUM | 3 | Moderate operational impact | Process optimization |
| HIGH | 4 | Significant operational impact | Equipment upgrades |
| CRITICAL | 5 | Mission-critical impact | Safety systems, core operations |

**Normalization**: Direct mapping to score.

**Contribution Example**:
- CRITICAL → 5 × 0.25 = **1.25**
- HIGH → 4 × 0.25 = **1.00**
- MEDIUM → 3 × 0.25 = **0.75**
- LOW → 1 × 0.25 = **0.25**

---

### 4. Risk (15% weight)

**Input**: Risk category

**Scale**:

| Risk | Score | Description | Example |
|------|-------|-------------|---------|
| LOW | 1 | Minimal risk | Administrative tasks |
| OPERATIONAL | 3 | Operational risk | Delays, inefficiency |
| SAFETY | 5 | Safety risk | Equipment failure, injury |
| LEGAL | 5 | Legal/compliance risk | Regulatory violations |

**Normalization**: Direct mapping to score.

**Contribution Example**:
- SAFETY → 5 × 0.15 = **0.75**
- LEGAL → 5 × 0.15 = **0.75**
- OPERATIONAL → 3 × 0.15 = **0.45**
- LOW → 1 × 0.15 = **0.15**

---

### 5. Strategic (5% weight)

**Input**: Strategic alignment score (future extension)

**Scale**: 1 to 5

**Current Implementation**: Default to 3.0 (neutral)

**Future Enhancements**:
- Align with organizational goals
- Track historical success rate of similar requests
- Weight by department or mission area

**Contribution Example**:
- Score 5 → 5 × 0.05 = **0.25**
- Score 3 → 3 × 0.05 = **0.15**
- Score 1 → 1 × 0.05 = **0.05**

---

## Complete Scoring Examples

### Example 1: Critical Safety Request

```
Request: "Emergency Vehicle Maintenance"
- Priority: 5 (Critical)
- Urgency: 42 days → 3.6
- Impact: CRITICAL (5)
- Risk: SAFETY (5)
- Strategic: 4

Score Calculation:
  (5 × 0.30) = 1.50
+ (3.6 × 0.25) = 0.90
+ (5 × 0.25) = 1.25
+ (5 × 0.15) = 0.75
+ (4 × 0.05) = 0.20
= 4.60
```

**Result**: Very high score, likely to be approved.

---

### Example 2: Standard Operational Request

```
Request: "Office Supply Restocking"
- Priority: 3 (Standard)
- Urgency: 60 days → 3.0
- Impact: MEDIUM (3)
- Risk: LOW (1)
- Strategic: 3

Score Calculation:
  (3 × 0.30) = 0.90
+ (3.0 × 0.25) = 0.75
+ (3 × 0.25) = 0.75
+ (1 × 0.15) = 0.15
+ (3 × 0.05) = 0.15
= 2.70
```

**Result**: Middle score, likely approved if budget allows.

---

### Example 3: Low Priority Request

```
Request: "Office Furniture Upgrade"
- Priority: 2 (Low)
- Urgency: 120 days → 1.0
- Impact: LOW (1)
- Risk: LOW (1)
- Strategic: 2

Score Calculation:
  (2 × 0.30) = 0.60
+ (1.0 × 0.25) = 0.25
+ (1 × 0.25) = 0.25
+ (1 × 0.15) = 0.15
+ (2 × 0.05) = 0.10
= 1.35
```

**Result**: Low score, likely deferred or denied unless budget surplus.

---

## Weight Justification

### Why These Weights?

| Component | Weight | Rationale |
|-----------|--------|-----------|
| Priority | 30% | Reflects strategic/organizational importance |
| Urgency | 25% | Time sensitivity is critical in operations |
| Impact | 25% | Operational consequences matter most |
| Risk | 15% | Safety/legal risks are differentiators |
| Strategic | 5% | Future extension; currently minor |

### Alternative Weight Profiles

Different organizations may want different profiles:

**Profile: Safety First**
- Risk: 35%
- Priority: 25%
- Impact: 20%
- Urgency: 15%
- Strategic: 5%

**Profile: Budget Efficiency**
- Priority: 20%
- Impact: 30%
- Strategic: 20%
- Urgency: 20%
- Risk: 10%

**Profile: Crisis Response**
- Urgency: 40%
- Risk: 25%
- Impact: 20%
- Priority: 10%
- Strategic: 5%

---

## Score Breakdown for Explanations

Every request receives a detailed breakdown:

```json
{
  "totalScore": 4.60,
  "priority": {
    "value": 5,
    "weight": 0.30,
    "contribution": 1.50,
    "percentOfTotal": 32.6
  },
  "urgency": {
    "value": 3.6,
    "weight": 0.25,
    "contribution": 0.90,
    "percentOfTotal": 19.6,
    "daysUntilDeadline": 42
  },
  "impact": {
    "value": 5,
    "weight": 0.25,
    "contribution": 1.25,
    "percentOfTotal": 27.2,
    "category": "CRITICAL"
  },
  "risk": {
    "value": 5,
    "weight": 0.15,
    "contribution": 0.75,
    "percentOfTotal": 16.3,
    "category": "SAFETY"
  },
  "strategic": {
    "value": 4,
    "weight": 0.05,
    "contribution": 0.20,
    "percentOfTotal": 4.3
  }
}
```

---

## Performance

### Computation Complexity

- **Per Request**: O(1) - constant time
- **All Requests**: O(n) - linear with request count
- **Typical Time**: < 1ms per request on modern hardware

### Caching

Scores are computed once per allocation run and stored:
- In-memory during allocation
- Persisted in `AllocationResult` table
- Invalidated when request attributes change

---

## Score Stability

### Deterministic

Same inputs always produce same score. No randomness.

### Temporal Changes

Urgency score decreases over time as deadlines approach:
- Re-scoring needed if allocation delayed
- Implemented as: `NOW() - urgency_deadline`

### User Control

Users can influence score by:
- Adjusting priority (requires approval)
- Setting earlier urgency deadline (must be justified)
- Documenting higher impact/risk (requires evidence)

---

## Testing

### Unit Tests

```java
@Test
void testCriticalSafetyRequest() {
    Request req = Request.builder()
        .priority(5)
        .urgencyDeadline(LocalDate.now().plusDays(42))
        .impact(Impact.CRITICAL)
        .risk(Risk.SAFETY)
        .strategic(4)
        .build();
    
    double score = scoringEngine.calculateScore(req);
    
    assertThat(score).isBetween(4.5, 4.7);
}
```

### Edge Cases

- Expired deadline → urgency score = 5.0
- Missing urgency → use default (90 days)
- Invalid priority → validation error
- Null impact → default to MEDIUM

---

## Future Enhancements

1. **Machine Learning Scoring**
   - Learn from historical approvals
   - Predict likelihood of success
   - Auto-adjust weights per department

2. **Dynamic Weights**
   - Adjust weights based on cycle phase
   - Seasonal adjustments
   - Crisis mode weight shifts

3. **Multi-Objective Optimization**
   - Pareto frontier analysis
   - Trade-off visualization
   - "What if" weight adjustment in UI

4. **Confidence Intervals**
   - Score uncertainty ranges
   - Sensitivity analysis
   - Robustness checks
