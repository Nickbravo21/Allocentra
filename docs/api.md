# Allocentra API Documentation

## Base URL

```
http://localhost:8080/api
```

## Authentication

Currently optional. Can add JWT authentication in Phase 2.

---

## Allocation Cycles

### Create Allocation Cycle

```http
POST /cycles
```

**Request Body:**

```json
{
  "name": "Q1 2026 Allocation",
  "description": "First quarter resource allocation cycle",
  "startDate": "2026-01-01",
  "endDate": "2026-03-31",
  "budgetPools": [
    {
      "category": "MONEY",
      "totalAmount": 500000.00,
      "currency": "USD"
    },
    {
      "category": "PERSONNEL",
      "totalAmount": 1000.0,
      "unit": "HOURS"
    }
  ],
  "resourcePools": [
    {
      "category": "VEHICLES",
      "resourceType": "TRUCK",
      "totalQuantity": 5,
      "availableHours": 2080
    }
  ]
}
```

**Response:** `201 Created`

```json
{
  "id": "01JGXXX...",
  "name": "Q1 2026 Allocation",
  "status": "DRAFT",
  "createdAt": "2026-01-03T10:00:00Z",
  "budgetPools": [...],
  "resourcePools": [...]
}
```

### Get Cycle Details

```http
GET /cycles/{cycleId}
```

**Response:** `200 OK`

```json
{
  "id": "01JGXXX...",
  "name": "Q1 2026 Allocation",
  "status": "ACTIVE",
  "totalBudget": 500000.00,
  "allocatedBudget": 320000.00,
  "remainingBudget": 180000.00,
  "requestCount": 47,
  "approvedCount": 23,
  "pendingCount": 24
}
```

---

## Requests

### Create Request

```http
POST /requests
```

**Request Body:**

```json
{
  "cycleId": "01JGXXX...",
  "title": "Emergency Vehicle Maintenance",
  "description": "Critical maintenance for fleet vehicles",
  "category": "MONEY",
  "amountRequested": 15000.00,
  "priority": 5,
  "urgencyDeadline": "2026-02-15",
  "impact": "CRITICAL",
  "risk": "SAFETY",
  "dependencies": [],
  "minimumViableAllocation": 10000.00,
  "justification": "Fleet safety inspection due"
}
```

**Response:** `201 Created`

```json
{
  "id": "01JGYYY...",
  "title": "Emergency Vehicle Maintenance",
  "status": "PENDING",
  "category": "MONEY",
  "amountRequested": 15000.00,
  "score": null,
  "createdAt": "2026-01-03T10:15:00Z"
}
```

### List Requests

```http
GET /requests?cycleId={cycleId}&status={status}&category={category}&page={page}&size={size}
```

**Query Parameters:**
- `cycleId` (required): Cycle ID
- `status` (optional): PENDING, APPROVED, PARTIAL, DEFERRED, DENIED
- `category` (optional): MONEY, PERSONNEL, VEHICLES, EQUIPMENT, HOURS
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response:** `200 OK`

```json
{
  "content": [
    {
      "id": "01JGYYY...",
      "title": "Emergency Vehicle Maintenance",
      "category": "MONEY",
      "amountRequested": 15000.00,
      "priority": 5,
      "urgencyDeadline": "2026-02-15",
      "impact": "CRITICAL",
      "risk": "SAFETY",
      "score": 4.75,
      "status": "PENDING"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 47,
  "totalPages": 3
}
```

---

## Allocation Runs

### Execute Allocation

```http
POST /runs
```

**Request Body:**

```json
{
  "cycleId": "01JGXXX...",
  "allowPartialAllocations": true,
  "categoryCaps": {
    "TRAINING": 0.25
  },
  "notes": "Standard Q1 allocation run"
}
```

**Response:** `202 Accepted`

```json
{
  "runId": "01JGZZZ...",
  "status": "RUNNING",
  "message": "Allocation engine started. Poll /runs/{runId} for results."
}
```

### Get Run Status and Results

```http
GET /runs/{runId}
```

**Response (Running):** `200 OK`

```json
{
  "runId": "01JGZZZ...",
  "status": "RUNNING",
  "progress": 0.45,
  "message": "Applying constraints..."
}
```

**Response (Completed):** `200 OK`

```json
{
  "runId": "01JGZZZ...",
  "status": "COMPLETED",
  "cycleId": "01JGXXX...",
  "completedAt": "2026-01-03T10:20:15Z",
  "executionTimeMs": 1247,
  "summary": {
    "totalRequests": 47,
    "approved": 23,
    "partial": 8,
    "deferred": 11,
    "denied": 5,
    "totalAllocated": 320000.00,
    "budgetUtilization": 0.64
  },
  "results": [
    {
      "requestId": "01JGYYY...",
      "status": "APPROVED",
      "amountRequested": 15000.00,
      "amountAllocated": 15000.00,
      "score": 4.75,
      "rank": 1
    }
  ]
}
```

---

## Explanations

### Get Decision Explanation

```http
GET /runs/{runId}/explanations/{requestId}
```

**Response:** `200 OK`

```json
{
  "requestId": "01JGYYY...",
  "requestTitle": "Emergency Vehicle Maintenance",
  "decision": "APPROVED",
  "amountAllocated": 15000.00,
  "rank": 1,
  "scoreBreakdown": {
    "totalScore": 4.75,
    "priority": { "value": 5, "weight": 0.30, "contribution": 1.50 },
    "urgency": { "value": 4.2, "weight": 0.25, "contribution": 1.05 },
    "impact": { "value": 5, "weight": 0.25, "contribution": 1.25 },
    "risk": { "value": 5, "weight": 0.15, "contribution": 0.75 },
    "strategic": { "value": 4, "weight": 0.05, "contribution": 0.20 }
  },
  "constraintsApplied": [],
  "reasonApproved": "Highest ranked request with critical safety impact. All constraints satisfied.",
  "comparisonToNext": {
    "nextRequestId": "01JGAAA...",
    "nextRequestTitle": "Training Program Extension",
    "nextRequestScore": 3.85,
    "scoreDifference": 0.90,
    "whyThisWon": "Higher urgency deadline (42 days vs 68 days) and critical safety risk"
  }
}
```

**Response (Denied):** `200 OK`

```json
{
  "requestId": "01JGBBB...",
  "requestTitle": "Office Furniture Upgrade",
  "decision": "DENIED",
  "amountAllocated": 0.00,
  "rank": 43,
  "scoreBreakdown": {
    "totalScore": 2.15,
    "priority": { "value": 2, "weight": 0.30, "contribution": 0.60 },
    "urgency": { "value": 1.8, "weight": 0.25, "contribution": 0.45 },
    "impact": { "value": 2, "weight": 0.25, "contribution": 0.50 },
    "risk": { "value": 1, "weight": 0.15, "contribution": 0.15 },
    "strategic": { "value": 3, "weight": 0.05, "contribution": 0.15 }
  },
  "constraintsApplied": ["BUDGET_EXHAUSTED"],
  "reasonDenied": "Budget fully allocated to higher-priority requests. Low urgency and impact.",
  "whatWouldChange": [
    "Increase priority to 4 or higher",
    "Move urgency deadline to within 30 days",
    "Demonstrate operational impact",
    "Request in next allocation cycle with earlier submission"
  ]
}
```

---

## Scenarios

### Create Scenario

```http
POST /scenarios
```

**Request Body:**

```json
{
  "baseCycleId": "01JGXXX...",
  "name": "Budget Cut 20%",
  "description": "Simulate 20% budget reduction",
  "modifications": {
    "budgetMultiplier": 0.80,
    "resourceMultipliers": {},
    "injectRequests": [],
    "removeRequests": []
  }
}
```

**Response:** `201 Created`

```json
{
  "scenarioId": "01JGCCC...",
  "name": "Budget Cut 20%",
  "status": "CREATED",
  "baseCycleId": "01JGXXX..."
}
```

### Run Scenario

```http
POST /scenarios/{scenarioId}/run
```

**Response:** `202 Accepted`

```json
{
  "scenarioRunId": "01JGDDD...",
  "status": "RUNNING"
}
```

### Get Scenario Results

```http
GET /scenarios/{scenarioId}/results
```

**Response:** `200 OK`

```json
{
  "scenarioId": "01JGCCC...",
  "name": "Budget Cut 20%",
  "status": "COMPLETED",
  "baseline": {
    "approved": 23,
    "partial": 8,
    "deferred": 11,
    "denied": 5,
    "totalAllocated": 320000.00
  },
  "scenario": {
    "approved": 18,
    "partial": 6,
    "deferred": 15,
    "denied": 8,
    "totalAllocated": 256000.00
  },
  "deltas": {
    "approvedDelta": -5,
    "partialDelta": -2,
    "deferredDelta": +4,
    "deniedDelta": +3,
    "allocationDelta": -64000.00
  },
  "impactedRequests": [
    {
      "requestId": "01JGEEE...",
      "title": "Training Program Extension",
      "baselineStatus": "APPROVED",
      "scenarioStatus": "PARTIAL",
      "reason": "Budget constraint reduced allocation from 12000 to 8000"
    }
  ]
}
```

---

## Audit

### Get Run History

```http
GET /audit?cycleId={cycleId}&userId={userId}&fromDate={date}&toDate={date}
```

**Response:** `200 OK`

```json
{
  "runs": [
    {
      "runId": "01JGZZZ...",
      "cycleId": "01JGXXX...",
      "cycleName": "Q1 2026 Allocation",
      "userId": "user@example.com",
      "timestamp": "2026-01-03T10:20:15Z",
      "executionTimeMs": 1247,
      "inputSnapshot": {
        "requestCount": 47,
        "totalBudget": 500000.00,
        "allowPartialAllocations": true
      },
      "summary": {
        "approved": 23,
        "partial": 8,
        "deferred": 11,
        "denied": 5
      },
      "engineVersion": "1.0.0"
    }
  ]
}
```

---

## Error Responses

### 400 Bad Request

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid request data",
  "details": {
    "amountRequested": "Must be greater than 0",
    "urgencyDeadline": "Must be a future date"
  }
}
```

### 404 Not Found

```json
{
  "error": "NOT_FOUND",
  "message": "Cycle with ID 01JGXXX... not found"
}
```

### 409 Conflict

```json
{
  "error": "CONFLICT",
  "message": "Allocation already running for this cycle",
  "runId": "01JGZZZ..."
}
```

### 500 Internal Server Error

```json
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2026-01-03T10:20:15Z",
  "requestId": "req-123abc"
}
```

---

## Rate Limiting

- **General endpoints**: 100 requests/minute per IP
- **Allocation runs**: 5 requests/minute per user
- **Scenario runs**: 10 requests/minute per user

---

## Pagination

List endpoints support pagination:

```
?page=0&size=20&sort=score,desc
```

Response includes:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 47,
  "totalPages": 3,
  "first": true,
  "last": false
}
```
