# Allocentra

**Professional Resource Allocation System with Full Explainability**

Allocentra takes limited budgets and resources (people, vehicles, equipment, hours) alongside competing requests, then produces allocation plans with ranked justifications, audit trails, and scenario simulations.

## 🎯 Core Features

- **Request Intake**: Multi-category requests (money, personnel, vehicles, equipment, hours) with priority, urgency, impact, risk, and dependencies
- **Allocation Engine**: Intelligent scoring and constraint-based allocation with explainability
- **Explanation Engine**: Complete score breakdown, constraint analysis, and decision justification
- **Scenario Simulation**: What-if analysis for budget cuts, surge events, resource loss
- **Audit Logging**: Complete trail of who ran what, when, with input snapshots

## 🏗️ Architecture

### Backend (Java 21 + Spring Boot 3)

```
backend/
├── src/main/java/com/allocentra/
│   ├── domain/           # Core entities
│   │   ├── Request
│   │   ├── ResourcePool
│   │   ├── BudgetPool
│   │   ├── AllocationCycle
│   │   ├── AllocationResult
│   │   ├── DecisionExplanation
│   │   └── AuditRun
│   ├── allocator/        # Allocation engine
│   ├── scoring/          # Scoring logic
│   ├── constraints/      # Constraint rules
│   ├── explanations/     # Explanation generator
│   ├── scenarios/        # Scenario engine
│   ├── api/              # REST controllers
│   ├── dto/              # API contracts
│   ├── repository/       # JPA repositories
│   └── service/          # Business logic
└── src/main/resources/
    ├── db/migration/     # Flyway migrations
    └── application.yml
```

### Frontend (React + TypeScript + Vite)

```
frontend/
├── src/
│   ├── components/       # Reusable UI components
│   ├── pages/           # Main application pages
│   │   ├── Dashboard
│   │   ├── Requests
│   │   ├── ResourcePools
│   │   ├── RunAllocation
│   │   ├── ResultsViewer
│   │   ├── ScenarioLab
│   │   └── AuditLog
│   ├── api/             # API client (TanStack Query)
│   ├── hooks/           # Custom React hooks
│   ├── types/           # TypeScript types (Zod schemas)
│   └── theme/           # Ops Command Center theme
```

## 🎨 UI Theme: Ops Command Center

Dark mode operational dashboard with:
- Big status cards with readiness indicators
- Real-time allocation status
- Priority ladder visualization
- Alert and constraint violation panels
- Side-by-side scenario comparison

## 📊 Scoring Formula

```
Score = (Priority * 0.30) + (Urgency * 0.25) + (Impact * 0.25) + (Risk * 0.15) + (Strategic * 0.05)

Where:
- Priority: 1-5 (manual)
- Urgency: days until deadline (normalized)
- Impact: LOW=1, MEDIUM=3, HIGH=4, CRITICAL=5
- Risk: SAFETY/LEGAL=5, OPERATIONAL=3, LOW=1
- Strategic: alignment score (future extension)
```

## 🔧 Tech Stack

**Backend**
- Java 21
- Spring Boot 3
- PostgreSQL with Flyway migrations
- JPA (Hibernate)
- OpenAPI/Swagger
- JUnit 5 + Testcontainers

**Frontend**
- React 18 + TypeScript
- Vite
- TanStack Query (React Query)
- Zod for validation
- Tailwind CSS
- shadcn/ui components

**Deployment**
- Backend: Render / Fly.io
- Database: Supabase / Railway
- Frontend: Vercel / Netlify

## 🚀 Quick Start

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

API available at `http://localhost:8080`
Swagger UI at `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App available at `http://localhost:5173`

## 📖 Documentation

- [API Contract](docs/api.md) - Complete endpoint specifications
- [Scoring Rules](docs/scoring.md) - Scoring formula and weights
- [Constraints](docs/constraints.md) - Constraint types and logic
- [Scenarios](docs/scenarios.md) - Scenario parameters and simulation

## 🧪 Testing

Backend includes comprehensive tests:
- Unit tests for scoring, constraints, allocation
- Integration tests with Testcontainers (real PostgreSQL)
- Demo dataset loader for testing

## 📈 Performance

- **Algorithmic**: O(n log n) for sorting, O(n) for allocation
- **Database**: Indexed on cycle_id and status
- **Async Execution**: Long-running allocations don't block UI
- **Efficient for**: Thousands of requests per cycle

## 🔐 Audit Trail

Every allocation run stores:
- Complete input snapshot
- Engine version used
- User who ran it
- Results and explanations
- Timestamp

## 📋 Key Endpoints

```
POST   /api/cycles                    Create allocation cycle
GET    /api/cycles/{id}               Get cycle details
POST   /api/requests                  Create request
GET    /api/requests?cycleId={id}     List requests for cycle
POST   /api/runs                      Run allocation (returns runId)
GET    /api/runs/{id}                 Get run results + explanations
POST   /api/scenarios                 Create scenario
GET    /api/scenarios/{id}            Get scenario results
GET    /api/audit?cycleId={id}        Get run history
```

## 🎯 MVP Scope

**Phase 1**: Core allocation engine + basic API
**Phase 2**: Frontend with request intake and results viewer
**Phase 3**: Scenario simulation + explanation UI
**Phase 4**: Audit log + polish + demo data

## 📝 License

MIT

---

**Built for real-world decision-making: defensible, repeatable, reviewable.**
