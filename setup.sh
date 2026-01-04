#!/bin/bash

# Allocentra Complete Setup Script

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║                    ALLOCENTRA SETUP                        ║"
echo "║   Professional Resource Allocation Command Center         ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "${BLUE}[1/5]${NC} Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed${NC}"
    echo "   Please install Java 21 or higher"
    echo "   https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo -e "${RED}❌ Java 21 or higher is required${NC}"
    echo "   Current version: $JAVA_VERSION"
    exit 1
fi

echo -e "${GREEN}✅ Java $JAVA_VERSION installed${NC}"

# Check Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js is not installed${NC}"
    echo "   Please install Node.js 18 or higher"
    echo "   https://nodejs.org/"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'.' -f1 | sed 's/v//')
if [ "$NODE_VERSION" -lt 18 ]; then
    echo -e "${RED}❌ Node.js 18 or higher is required${NC}"
    echo "   Current version: v$NODE_VERSION"
    exit 1
fi

echo -e "${GREEN}✅ Node.js $(node -v) installed${NC}"

# Check PostgreSQL (optional)
if command -v psql &> /dev/null; then
    echo -e "${GREEN}✅ PostgreSQL installed${NC}"
else
    echo -e "${YELLOW}⚠️  PostgreSQL not found (will use H2 for dev)${NC}"
fi

echo ""
echo -e "${BLUE}[2/5]${NC} Setting up backend..."

cd backend

# Make run script executable
chmod +x run.sh

# Check if Maven wrapper exists
if [ ! -f "mvnw" ]; then
    echo "   Generating Maven wrapper..."
    mvn -N wrapper:wrapper
fi

chmod +x mvnw

# Download dependencies (don't run yet)
echo "   Downloading dependencies..."
./mvnw dependency:resolve -q || true

echo -e "${GREEN}✅ Backend configured${NC}"

cd ..

echo ""
echo -e "${BLUE}[3/5]${NC} Setting up frontend..."

cd frontend

# Make run script executable
chmod +x run.sh

# Install npm dependencies
echo "   Installing npm packages..."
npm install --silent

echo -e "${GREEN}✅ Frontend configured${NC}"

cd ..

echo ""
echo -e "${BLUE}[4/5]${NC} Creating PostgreSQL database (optional)..."

# Try to create database
if command -v psql &> /dev/null; then
    echo "   Attempting to create database 'allocentra'..."
    
    psql -U postgres -c "CREATE DATABASE allocentra;" 2>/dev/null && \
    psql -U postgres -c "CREATE USER allocentra WITH PASSWORD 'allocentra';" 2>/dev/null && \
    psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE allocentra TO allocentra;" 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ PostgreSQL database created${NC}"
    else
        echo -e "${YELLOW}⚠️  Database setup skipped (may already exist or permission denied)${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  PostgreSQL not available, skipping database creation${NC}"
    echo "   Backend will use H2 in-memory database for development"
fi

echo ""
echo -e "${BLUE}[5/5]${NC} Creating run scripts..."

# Create main run script
cat > run-all.sh << 'EOF'
#!/bin/bash

echo "Starting Allocentra..."

# Start backend in background
echo "Starting backend on http://localhost:8080"
cd backend && ./mvnw spring-boot:run &
BACKEND_PID=$!

# Wait for backend to start
echo "Waiting for backend to initialize..."
sleep 15

# Start frontend
echo "Starting frontend on http://localhost:5173"
cd ../frontend && npm run dev

# Cleanup on exit
trap "kill $BACKEND_PID" EXIT
EOF

chmod +x run-all.sh

echo -e "${GREEN}✅ Run scripts created${NC}"

echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║                   SETUP COMPLETE! 🎉                      ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""
echo -e "${GREEN}Your Allocentra installation is ready!${NC}"
echo ""
echo "To start the full application:"
echo -e "  ${BLUE}./run-all.sh${NC}"
echo ""
echo "Or start components individually:"
echo -e "  Backend:  ${BLUE}cd backend && ./run.sh${NC}"
echo -e "  Frontend: ${BLUE}cd frontend && ./run.sh${NC}"
echo ""
echo "Access points:"
echo -e "  🌐 Frontend:  ${BLUE}http://localhost:5173${NC}"
echo -e "  🔧 Backend:   ${BLUE}http://localhost:8080${NC}"
echo -e "  📚 API Docs:  ${BLUE}http://localhost:8080/swagger-ui.html${NC}"
echo ""
echo "Documentation:"
echo "  📖 README.md - Project overview"
echo "  📖 docs/api.md - Complete API reference"
echo "  📖 docs/scoring.md - Scoring algorithm"
echo "  📖 docs/constraints.md - Constraint rules"
echo "  📖 docs/scenarios.md - Scenario simulation"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "  1. Review the documentation in docs/"
echo "  2. Start the application with ./run-all.sh"
echo "  3. Open http://localhost:5173 in your browser"
echo "  4. Create an allocation cycle"
echo "  5. Add requests and run allocation"
echo ""
echo "Happy allocating! 🚀"
