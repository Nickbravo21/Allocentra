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
