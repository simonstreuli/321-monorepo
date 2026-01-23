# Order Service - API Versioning and Docker Pipeline

## API Versioning

The Order Service now supports three API endpoints for backward compatibility and future enhancements:

### 1. Legacy Endpoint (Backward Compatible)
- **Endpoint**: `POST /orders`
- **Response**: Standard OrderResponse
```json
{
  "orderId": "uuid",
  "status": "SUCCESS|ERROR|PAYMENT_FAILED",
  "message": "Status message"
}
```

### 2. V1 API
- **Endpoint**: `POST /api/v1/orders`
- **Health**: `GET /api/v1/orders/health`
- **Response**: Same as legacy endpoint (OrderResponse)
```json
{
  "orderId": "uuid",
  "status": "SUCCESS|ERROR|PAYMENT_FAILED",
  "message": "Status message"
}
```

### 3. V2 API (Enhanced)
- **Endpoint**: `POST /api/v2/orders`
- **Health**: `GET /api/v2/orders/health`
- **Response**: Enhanced response with additional metadata
```json
{
  "orderId": "uuid",
  "status": "SUCCESS|ERROR|PAYMENT_FAILED",
  "message": "Status message",
  "apiVersion": "v2",
  "timestamp": "2026-01-23T09:04:05.946876053"
}
```

**V2 Health Response**:
```json
{
  "status": "UP",
  "service": "Order Service",
  "version": "v2",
  "timestamp": "2026-01-23T09:04:05.946876053"
}
```

## Testing the APIs

### Using curl:

```bash
# Test V1 API
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"pizza": "Margherita", "quantity": 2, "address": "Test Street 123", "customerName": "Test User"}'

# Test V2 API
curl -X POST http://localhost:8080/api/v2/orders \
  -H "Content-Type: application/json" \
  -d '{"pizza": "Pepperoni", "quantity": 1, "address": "Test Street 456", "customerName": "Test User 2"}'

# Test Legacy API (backward compatible)
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"pizza": "Hawaii", "quantity": 3, "address": "Test Street 789", "customerName": "Test User 3"}'
```

## Docker Pipeline

A GitHub Actions workflow has been created to automatically build and push Docker images for the Order Service.

### Workflow File
`.github/workflows/order-service-docker.yml`

### Features:
- **Automatic builds** on push to `main` or `develop` branches
- **Pushes to GitHub Container Registry** (ghcr.io)
- **Platform support**: linux/amd64
- **Build caching** for faster builds
- **Semantic versioning** support with tags
- **Pull request validation** (builds but doesn't push)

### Tags Generated:
- `latest` - for main branch
- `<branch-name>` - for branch builds (e.g., `develop`, `main`)
- `sha-<commit-sha>` - for specific commits (e.g., `sha-abc1234`)
- Semantic version tags when using semver releases (e.g., `v1.0.0`, `v1.0`, `v1`)

### Triggering the Workflow:
1. **Automatic**: Push changes to `main` or `develop` that affect:
   - `order-service/**`
   - `pizza-models/**`
   - `.github/workflows/order-service-docker.yml`

2. **Manual**: Use GitHub Actions UI to trigger `workflow_dispatch`

### Using the Docker Image:

```bash
# Pull the image
docker pull ghcr.io/simonstreuli/order-service:latest

# Run the container
docker run -p 8080:8080 \
  -e SPRING_RABBITMQ_HOST=rabbitmq \
  -e PAYMENT_SERVICE_URL=http://payment-service:8081 \
  ghcr.io/simonstreuli/order-service:latest
```

## Local Development

### Build locally:
```bash
# Build pizza-models first
cd pizza-models
mvn clean install

# Then build order-service
cd ../order-service
mvn clean package
```

### Run locally:
```bash
cd order-service
mvn spring-boot:run
```

## Migration Guide

### For Existing Clients:
- **No changes required**: The legacy `/orders` endpoint continues to work
- **Recommended**: Migrate to `/api/v1/orders` for explicit versioning
- **Future-proof**: Consider `/api/v2/orders` for enhanced features

### For New Integrations:
- Use `/api/v2/orders` for new integrations to get enhanced metadata
- Use `/api/v1/orders` if you need the simpler response format

## Future Enhancements

V3 API could include:
- Estimated delivery time
- Real-time order tracking
- Payment method details
- Detailed pricing breakdown
- Order history integration
