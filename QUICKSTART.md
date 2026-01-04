# QUICKSTART.md

# Allocentra Quick Start Guide

Get Allocentra running in under 5 minutes.

## Prerequisites

Before starting, ensure you have:

- ✅ **Java 21+** ([Download](https://adoptium.net/))
- ✅ **Node.js 18+** ([Download](https://nodejs.org/))
- ⚠️  **PostgreSQL 15+** (Optional - H2 used by default)

Verify installations:

```bash
java -version   # Should show 21 or higher
node -v         # Should show v18 or higher
```

## Automated Setup

Run the setup script:

```bash
chmod +x setup.sh
./setup.sh
```

This will:
1. Check prerequisites
2. Set up backend dependencies
3. Install frontend packages
4. Create database (if PostgreSQL available)
5. Generate run scripts

## Start Allocentra

### Option 1: Start Everything (Recommended)

```bash
./run-all.sh
```

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- API Docs: `http://localhost:8080/swagger-ui.html`

### Option 2: Start Components Separately

**Terminal 1 - Backend:**
```bash
cd backend
./run.sh
```

**Terminal 2 - Frontend:**
```bash
cd frontend
./run.sh
```

## First Steps

1. **Open the app**: Navigate to `http://localhost:5173`

2. **Create an Allocation Cycle:**
   - Click "Cycles" in the sidebar
   - Create a new cycle (e.g., "Q1 2026 Budget")
   - Add budget pools and resource pools

3. **Submit Requests:**
   - Click "Requests"
   - Create sample requests with different priorities
   - Set urgency deadlines and impact levels

4. **Run Allocation:**
   - Click "Run Allocation"
   - Select your cycle
   - Execute the allocation engine

5. **View Results:**
   - Click "Results"
   - See which requests were approved/denied
   - View detailed explanations for each decision

## Database Configuration

### Option A: H2 (Default - No Setup Required)

Backend uses H2 in-memory database automatically.

Access H2 Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:allocentra`
- Username: `sa`
- Password: (leave empty)

### Option B: PostgreSQL (Production)

Create database:

```bash
psql -U postgres
CREATE DATABASE allocentra;
CREATE USER allocentra WITH PASSWORD 'allocentra';
GRANT ALL PRIVILEGES ON DATABASE allocentra TO allocentra;
\q
```

Set environment variables:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/allocentra
export DATABASE_USERNAME=allocentra
export DATABASE_PASSWORD=allocentra
```

Or edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/allocentra
    username: allocentra
    password: allocentra
```

## Architecture Overview

```
Allocentra/
├── backend/              # Java 21 + Spring Boot 3
│   ├── src/
│   │   ├── domain/       # Entities (Request, Cycle, Result, etc.)
│   │   ├── allocator/    # Allocation engine
│   │   ├── scoring/      # Scoring logic
│   │   ├── api/          # REST controllers
│   │   └── repository/   # Data access
│   └── pom.xml           # Maven config
│
├── frontend/             # React + TypeScript + Vite
│   ├── src/
│   │   ├── pages/        # Main views
│   │   ├── components/   # UI components
│   │   ├── lib/          # API client, utilities
│   │   └── main.tsx      # Entry point
│   └── package.json      # npm config
│
└── docs/                 # Complete documentation
    ├── api.md            # API reference
    ├── scoring.md        # Scoring algorithm
    ├── constraints.md    # Constraint rules
    └── scenarios.md      # Scenario simulation
```

## Allocation Flow

```
1. Create Cycle
   ↓
2. Define Budget/Resource Pools
   ↓
3. Submit Requests
   ↓
4. Execute Allocation Engine
   ↓
5. View Results + Explanations
   ↓
6. (Optional) Run Scenarios
```

## Key Concepts

### Scoring Formula

```
Score = (Priority × 0.30) + (Urgency × 0.25) + 
        (Impact × 0.25) + (Risk × 0.15) + (Strategic × 0.05)
```

- **Priority**: 1-5 (user-defined importance)
- **Urgency**: Days until deadline (closer = higher)
- **Impact**: LOW, MEDIUM, HIGH, CRITICAL
- **Risk**: LOW, OPERATIONAL, SAFETY, LEGAL
- **Strategic**: 1-5 (alignment score)

### Allocation Constraints

1. **Budget** - Cannot exceed available funds
2. **Resources** - Cannot over-allocate vehicles/equipment
3. **Dependencies** - Dependent requests wait for prerequisites
4. **Category Caps** - Max % of budget per category
5. **Minimum Viable** - Partial funding must meet threshold

### Request Statuses

- **PENDING** - Not yet processed
- **APPROVED** - Fully funded
- **PARTIAL** - Partially funded (if allowed)
- **DEFERRED** - Dependency not met
- **DENIED** - Not funded

## API Endpoints

### Core Operations

```bash
# Health check
curl http://localhost:8080/api/health

# Create cycle
curl -X POST http://localhost:8080/api/cycles \
  -H "Content-Type: application/json" \
  -d '{"name": "Q1 2026", "startDate": "2026-01-01", "endDate": "2026-03-31"}'

# Create request
curl -X POST http://localhost:8080/api/requests \
  -H "Content-Type: application/json" \
  -d '{"title": "Vehicle Maintenance", "category": "MONEY", "amountRequested": 15000}'

# Run allocation
curl -X POST http://localhost:8080/api/runs \
  -H "Content-Type: application/json" \
  -d '{"cycleId": "YOUR_CYCLE_ID"}'

# Get results
curl http://localhost:8080/api/runs/YOUR_RUN_ID
```

Complete API documentation: `http://localhost:8080/swagger-ui.html`

## Development

### Backend

```bash
cd backend

# Run with hot reload
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build JAR
./mvnw clean package
```

### Frontend

```bash
cd frontend

# Development server
npm run dev

# Production build
npm run build

# Preview build
npm run preview
```

## Troubleshooting

### Backend won't start

**Issue**: Port 8080 already in use
```bash
# Find process using port 8080
lsof -i :8080
# Kill it
kill -9 <PID>
```

**Issue**: Java version error
```bash
# Verify Java version
java -version
# Should be 21 or higher
```

### Frontend won't start

**Issue**: Port 5173 already in use
```bash
# Kill process on port 5173
lsof -i :5173 | grep LISTEN | awk '{print $2}' | xargs kill -9
```

**Issue**: Module not found
```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

### Database connection error

**Issue**: PostgreSQL connection failed
```bash
# Check PostgreSQL is running
pg_isready

# Verify connection
psql -U allocentra -d allocentra -c "SELECT 1;"
```

**Solution**: Use H2 for development
```bash
# Run with dev profile (uses H2)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Next Steps

1. **Read the docs** - Check `docs/` folder for detailed guides
2. **Explore API** - Use Swagger UI to test endpoints
3. **Customize scoring** - Adjust weights in `application.yml`
4. **Add scenarios** - Test budget cuts and surge events
5. **Deploy** - See deployment guides in backend/frontend READMEs

## Support

- 📖 **Documentation**: Check the `docs/` folder
- 🐛 **Issues**: Open an issue on GitHub
- 💬 **Discussions**: Join the community

## What Makes Allocentra Real

✅ **Explainability** - Every decision has a detailed breakdown
✅ **Audit Trail** - Complete history of who ran what, when
✅ **Scenario Simulation** - What-if analysis for planning
✅ **Constraint-Based** - Real-world rules enforced
✅ **Production-Ready** - PostgreSQL, JPA, proper architecture
✅ **Professional UI** - Dark ops theme, status indicators

This isn't a toy - it's built for real decision-making.

---

**Built with**:
- Backend: Java 21, Spring Boot 3, PostgreSQL, Flyway
- Frontend: React 18, TypeScript, Vite, Tailwind CSS, TanStack Query
- Architecture: Clean separation, RESTful API, OpenAPI docs

Happy allocating! 🚀
