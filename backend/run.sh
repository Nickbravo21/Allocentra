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

# Check if Maven wrapper exists
if [ ! -f "mvnw" ]; then
    echo "❌ Maven wrapper not found. Please ensure you're in the backend directory."
    exit 1
fi

# Make Maven wrapper executable
chmod +x mvnw

# Run the application
echo "📦 Building and running Allocentra..."
./mvnw clean spring-boot:run
