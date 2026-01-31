#!/bin/bash

# Allocentra Backend Launcher

echo "🚀 Starting Allocentra Backend..."

# Check if Java 21 is available
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java 21 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $JAVA_VERSION"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven."
    exit 1
fi

echo "✅ Maven is available"

# Run the application with dev profile (uses H2 in-memory database)
echo "📦 Building and running Allocentra with dev profile..."
echo "🔍 Using H2 in-memory database"
echo "🌐 API will be available at http://localhost:8080"
echo "📖 API docs will be at http://localhost:8080/swagger-ui.html"
echo ""
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
