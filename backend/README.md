# Allocentra Backend

Professional resource allocation API built with:
- **Java 21** + **Spring Boot 3**
- **PostgreSQL** with Flyway migrations
- **OpenAPI/Swagger** documentation

## Quick Start

### Prerequisites

- Java 21 or higher
- PostgreSQL 15+ (or use H2 for dev)
- Maven 3.8+

### Run Locally

```bash
./run.sh
```

Or with Maven:

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI Spec: `http://localhost:8080/api-docs`

### Database

**PostgreSQL** (Production):
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/allocentra
export DATABASE_USERNAME=allocentra
export DATABASE_PASSWORD=allocentra
```

**H2** (Development):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

H2 Console: `http://localhost:8080/h2-console`

### Run Tests

```bash
./mvnw test
```

## Project Structure

```
src/main/java/com/allocentra/
├── AllocentraApplication.java    # Main entry point
├── domain/                        # Entities
│   ├── AllocationCycle.java
│   ├── Request.java
│   ├── AllocationRun.java
│   ├── AllocationResult.java
│   └── DecisionExplanation.java
├── allocator/                     # Allocation engine
│   ├── AllocationEngine.java
│   └── ConstraintEngine.java
├── scoring/                       # Scoring logic
│   └── ScoringEngine.java
├── repository/                    # Data access
├── api/                          # REST controllers
│   └── AllocentraController.java
└── config/                       # Configuration
    └── OpenApiConfig.java
```

## Key Endpoints

### Cycles

- `POST /api/cycles` - Create allocation cycle
- `GET /api/cycles/{id}` - Get cycle details
- `GET /api/cycles` - List all cycles

### Requests

- `POST /api/requests` - Create request
- `GET /api/requests?cycleId={id}` - List requests
- `GET /api/requests/{id}` - Get request details

### Allocation Runs

- `POST /api/runs` - Execute allocation (async)
- `GET /api/runs/{id}` - Get run status and results
- `GET /api/runs` - List all runs

### Health

- `GET /api/health` - Health check

## Configuration

Key properties in `application.yml`:

```yaml
allocentra:
  engine:
    version: "1.0.0"
    async-execution: true
    max-requests-per-cycle: 1000
  
  scoring:
    weights:
      priority: 0.30
      urgency: 0.25
      impact: 0.25
      risk: 0.15
      strategic: 0.05
  
  constraints:
    default-allow-partial: true
    enforce-dependencies: true
```

## Development

### Build

```bash
./mvnw clean package
```

### Run with profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Generate Maven Wrapper (if missing)

```bash
mvn -N wrapper:wrapper
```

## Deployment

### Docker

```bash
docker build -t allocentra-backend .
docker run -p 8080:8080 allocentra-backend
```

### Environment Variables

- `DATABASE_URL` - JDBC connection string
- `DATABASE_USERNAME` - Database user
- `DATABASE_PASSWORD` - Database password
- `SERVER_PORT` - Port (default: 8080)

## License

MIT
