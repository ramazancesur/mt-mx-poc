#!/bin/bash

# MT-MX Project Setup Script
# This script sets up the entire MT-MX conversion project with one command

set -e

echo "🚀 Starting MT-MX Project Setup..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
check_docker() {
    print_status "Checking Docker installation..."
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    print_success "Docker and Docker Compose are installed"
}

# Check if Java is installed
check_java() {
    print_status "Checking Java installation..."
    if ! command -v java &> /dev/null; then
        print_warning "Java is not installed. Docker will handle Java runtime."
    else
        java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
        print_success "Java version: $java_version"
    fi
}

# Check if Node.js is installed
check_node() {
    print_status "Checking Node.js installation..."
    if ! command -v node &> /dev/null; then
        print_warning "Node.js is not installed. Docker will handle Node.js runtime."
    else
        node_version=$(node --version)
        print_success "Node.js version: $node_version"
    fi
}

# Clean up previous containers and volumes
cleanup() {
    print_status "Cleaning up previous containers and volumes..."
    docker-compose down --volumes --remove-orphans 2>/dev/null || true
    docker system prune -f 2>/dev/null || true
    print_success "Cleanup completed"
}

# Build and start services
start_services() {
    print_status "Building and starting services..."
    
    # Build images
    print_status "Building Docker images..."
    docker-compose build --no-cache
    
    # Start services
    print_status "Starting services..."
    docker-compose up -d
    
    print_success "Services started successfully"
}

# Wait for services to be healthy
wait_for_services() {
    print_status "Waiting for services to be healthy..."
    
    # Wait for database
    print_status "Waiting for database..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if docker-compose exec -T db pg_isready -U user -d mtmxdb &>/dev/null; then
            print_success "Database is ready"
            break
        fi
        sleep 2
        timeout=$((timeout-2))
    done
    
    if [ $timeout -le 0 ]; then
        print_error "Database failed to start within 60 seconds"
        exit 1
    fi
    
    # Wait for backend
    print_status "Waiting for backend..."
    timeout=120
    while [ $timeout -gt 0 ]; do
        if curl -f http://localhost:8081/actuator/health &>/dev/null; then
            print_success "Backend is ready"
            break
        fi
        sleep 3
        timeout=$((timeout-3))
    done
    
    if [ $timeout -le 0 ]; then
        print_error "Backend failed to start within 120 seconds"
        exit 1
    fi
    
    # Wait for frontend
    print_status "Waiting for frontend..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if curl -f http://localhost:3000/health &>/dev/null; then
            print_success "Frontend is ready"
            break
        fi
        sleep 2
        timeout=$((timeout-2))
    done
    
    if [ $timeout -le 0 ]; then
        print_error "Frontend failed to start within 60 seconds"
        exit 1
    fi
}

# Show service status
show_status() {
    print_status "Service Status:"
    docker-compose ps
    
    echo ""
    print_success "🎉 MT-MX Project is now running!"
    echo ""
    echo "📱 Frontend: http://localhost:3000"
    echo "🔧 Backend API: http://localhost:8081"
    echo "📚 Swagger UI: http://localhost:8081/swagger-ui.html"
    echo "🏥 Health Check: http://localhost:8081/actuator/health"
    echo ""
    print_status "To stop the services, run: docker-compose down"
    print_status "To view logs, run: docker-compose logs -f"
}

# Run tests
run_tests() {
    print_status "Running backend tests..."
    if docker-compose exec -T backend mvn test; then
        print_success "All tests passed"
    else
        print_warning "Some tests failed, but services are still running"
    fi
}

# Main execution
main() {
    echo "╔══════════════════════════════════════╗"
    echo "║        MT-MX Project Setup           ║"
    echo "║   SWIFT Message Conversion System    ║"
    echo "╚══════════════════════════════════════╝"
    echo ""
    
    check_docker
    check_java
    check_node
    cleanup
    start_services
    wait_for_services
    run_tests
    show_status
    
    print_success "Setup completed successfully! 🚀"
}

# Handle script interruption
trap 'print_error "Setup interrupted"; docker-compose down; exit 1' INT TERM

# Run main function
main

exit 0 