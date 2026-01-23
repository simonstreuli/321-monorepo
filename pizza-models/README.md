# Pizza Models Library

This is a shared library containing all common data models used across the Pizza Delivery Platform services.

## Purpose

The pizza-models library solves the problem of model duplication across microservices. Instead of each service maintaining its own copy of shared models, they all depend on this centralized library. This ensures:

- **Consistency**: All services use the exact same model definitions
- **Maintainability**: Changes to models are made in one place
- **Type Safety**: Compile-time errors if services use incompatible model versions
- **Automatic Updates**: When the producer updates a model, consumers get the change automatically through dependency management

## Models Included

### Order Models
- **OrderRequest**: Request model for creating new orders (with validation)
- **OrderResponse**: Response model for order operations
- **OrderPlacedEvent**: Event published when an order is placed
- **OrderReadyEvent**: Event published when an order is ready for delivery

### Payment Models
- **PaymentRequest**: Request model for payment processing (with validation)
- **PaymentResponse**: Response model for payment operations

## Usage

### Adding as a Dependency

Add this dependency to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.pizza</groupId>
    <artifactId>pizza-models</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Building the Library

From the pizza-models directory:

```bash
mvn clean install
```

This will build the library and install it to your local Maven repository (~/.m2/repository), making it available to all services.

### Using the Models

Import the models in your Java code:

```java
import com.pizza.models.OrderRequest;
import com.pizza.models.OrderPlacedEvent;
import com.pizza.models.PaymentRequest;
// etc.
```

## Development

### Making Changes to Models

1. Update the model classes in `src/main/java/com/pizza/models/`
2. Build and install the library: `mvn clean install`
3. Update the version in dependent services if needed
4. Rebuild all affected services

### Version Management

When making breaking changes to models:
1. Increment the version in `pom.xml`
2. Update the version in all dependent services
3. Test all services together before deploying

## Dependencies

This library uses:
- **Lombok**: For reducing boilerplate code (@Data, @NoArgsConstructor, @AllArgsConstructor)
- **Jakarta Validation**: For input validation annotations (@NotBlank, @Positive, etc.)
- **Jackson**: For JSON serialization/deserialization (provided by Spring Boot)

## Services Using This Library

- order-service
- kitchen-service
- payment-service
