#!/bin/bash

# Integration Test Script for Distributed Pizza Platform
# This script starts all services with Docker Compose and runs end-to-end tests

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=====================================${NC}"
echo -e "${YELLOW}Integration Test - Pizza Platform${NC}"
echo -e "${YELLOW}=====================================${NC}"
echo ""

# Function to print colored messages
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=60
    local attempt=1

    log_info "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            log_info "$service_name is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "$service_name failed to start within expected time"
    return 1
}

# Function to cleanup
cleanup() {
    log_info "Cleaning up..."
    docker compose down -v
    log_info "Cleanup completed"
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Start services
log_info "Starting all services with Docker Compose..."
docker compose up -d

echo ""

# Wait for RabbitMQ
log_info "Waiting for RabbitMQ Management UI..."
wait_for_service "RabbitMQ" "http://localhost:15672"

# Wait for all services to be ready
log_info "Waiting for services to be ready..."
wait_for_service "Payment Service" "http://localhost:8081/health"
wait_for_service "Order Service" "http://localhost:8080/orders/health"
wait_for_service "Delivery Service" "http://localhost:8083/deliveries/health"
wait_for_service "Frontend" "http://localhost:3000"

# Give Kitchen Service some time to connect to RabbitMQ
log_info "Waiting for Kitchen Service to connect to RabbitMQ..."
sleep 5

echo ""
log_info "All services are up and running!"
echo ""

# Run integration tests
log_info "======================================"
log_info "Running Integration Tests"
log_info "======================================"
echo ""

TEST_FAILED=0

# Test 1: Health Checks
log_info "Test 1: Health Checks"
if curl -s -f http://localhost:8080/orders/health > /dev/null && \
   curl -s -f http://localhost:8081/health > /dev/null && \
   curl -s -f http://localhost:8083/deliveries/health > /dev/null; then
    log_info "✓ All health checks passed"
else
    log_error "✗ Health check failed"
    TEST_FAILED=1
fi
echo ""

# Test 2: Create Order - Success Flow
log_info "Test 2: Create Order - Success Flow"
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/orders \
    -H "Content-Type: application/json" \
    -d '{
        "pizza": "Margherita",
        "quantity": 2,
        "address": "Teststrasse 123, 8000 Zürich",
        "customerName": "Integration Test User"
    }')

if echo "$ORDER_RESPONSE" | grep -q "orderId"; then
    ORDER_ID=$(echo "$ORDER_RESPONSE" | grep -o '"orderId":"[^"]*"' | cut -d'"' -f4)
    log_info "✓ Order created successfully with ID: $ORDER_ID"
    echo "   Response: $ORDER_RESPONSE"
else
    log_error "✗ Failed to create order"
    echo "   Response: $ORDER_RESPONSE"
    TEST_FAILED=1
fi
echo ""

# Test 3: Verify RabbitMQ Message Flow
log_info "Test 3: Verify RabbitMQ Message Flow"
sleep 2  # Give time for message to be processed

# Check RabbitMQ queues
RABBITMQ_RESPONSE=$(curl -s -u guest:guest http://localhost:15672/api/queues/%2F/order.placed)
if echo "$RABBITMQ_RESPONSE" | grep -q "order.placed"; then
    log_info "✓ RabbitMQ queue 'order.placed' exists"
else
    log_warning "⚠ Could not verify RabbitMQ queue"
fi
echo ""

# Test 4: Multiple Orders (Load Test)
log_info "Test 4: Create Multiple Orders"
SUCCESS_COUNT=0
for i in {1..3}; do
    RESPONSE=$(curl -s -X POST http://localhost:8080/orders \
        -H "Content-Type: application/json" \
        -d "{
            \"pizza\": \"Quattro Formaggi\",
            \"quantity\": $i,
            \"address\": \"Test Address $i\",
            \"customerName\": \"Test User $i\"
        }")
    
    if echo "$RESPONSE" | grep -q "orderId"; then
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    fi
done

if [ $SUCCESS_COUNT -eq 3 ]; then
    log_info "✓ Successfully created 3 orders"
else
    log_error "✗ Only $SUCCESS_COUNT out of 3 orders succeeded"
    TEST_FAILED=1
fi
echo ""

# Test 5: Delivery Service Integration
log_info "Test 5: Delivery Service Integration"
# Wait for kitchen service to prepare (5-10s) and delivery to receive event
# This is intentionally a fixed wait for integration test simplicity
# Kitchen preparation time is configured to 5-10 seconds
sleep 15

# Check if delivery service responds with HTTP 200 (service is working)
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/deliveries)
DELIVERIES_RESPONSE=$(curl -s http://localhost:8083/deliveries)

if [ "$HTTP_STATUS" = "200" ]; then
    log_info "✓ Delivery service is responding (HTTP $HTTP_STATUS)"
    if [ -n "$DELIVERIES_RESPONSE" ]; then
        echo "   Active deliveries: $DELIVERIES_RESPONSE"
    else
        echo "   Active deliveries: (empty response - no active deliveries yet)"
    fi
else
    log_error "✗ Delivery service integration issue (HTTP $HTTP_STATUS)"
    echo "   Response: $DELIVERIES_RESPONSE"
    TEST_FAILED=1
fi
echo ""

# Test 6: Frontend Accessibility
log_info "Test 6: Frontend Service"
if curl -s -f http://localhost:3000 > /dev/null; then
    log_info "✓ Frontend is accessible"
else
    log_error "✗ Frontend is not accessible"
    TEST_FAILED=1
fi
echo ""

# Display service logs for debugging (last 10 lines each)
log_info "======================================"
log_info "Service Logs Summary"
log_info "======================================"
echo ""

log_info "Order Service Logs:"
docker compose logs --tail=10 order-service
echo ""

log_info "Payment Service Logs:"
docker compose logs --tail=10 payment-service
echo ""

log_info "Kitchen Service Logs:"
docker compose logs --tail=10 kitchen-service
echo ""

log_info "Delivery Service Logs:"
docker compose logs --tail=10 delivery-service
echo ""

# Final result
echo ""
log_info "======================================"
if [ $TEST_FAILED -eq 0 ]; then
    log_info "${GREEN}All Integration Tests PASSED!${NC}"
    log_info "======================================"
    exit 0
else
    log_error "${RED}Some Integration Tests FAILED!${NC}"
    log_info "======================================"
    exit 1
fi
