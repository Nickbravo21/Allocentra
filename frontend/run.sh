#!/bin/bash

# Allocentra Frontend Launcher

echo "🚀 Starting Allocentra Frontend..."

# Check if Node.js is available
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18 or higher."
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'.' -f1 | sed 's/v//')
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Node.js 18 or higher is required. Current version: $NODE_VERSION"
    exit 1
fi

echo "✅ Node.js version: $(node -v)"

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Run the development server
echo "🎨 Starting development server..."
npm run dev
