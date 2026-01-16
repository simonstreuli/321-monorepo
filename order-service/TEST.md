# Test Documentation for Order Service

## Overview

This document describes the comprehensive test suite for the Order Service. The tests are organized into three main categories: **Unit Tests**, **Integration Tests**, and **API Tests**. The test suite ensures the reliability, correctness, and robustness of the order processing system.

## Test Structure

```
src/test/java/com/pizza/order/
├── controller/
│   └── OrderControllerTest.java          # Unit tests for controller
├── service/
│   └── OrderServiceTest.java             # Unit tests for service layer
├── model/
│   └── OrderRequestTest.java             # Validation tests for models
└── integration/
    ├── OrderControllerIntegrationTest.java   # Integration tests with MockMvc
    ├── OrderServiceApiTest.java              # Full API end-to-end tests
    └── RabbitMQIntegrationTest.java          # RabbitMQ messaging tests
```

## Test Categories

### 1. Unit Tests

Unit tests verify individual components in isolation using mocks for dependencies.

#### OrderServiceTest

**Location:** `src/test/java/com/pizza/order/service/OrderServiceTest.java`

**Purpose:** Tests the business logic of order processing with mocked dependencies.

**Key Test Cases:**
- ✅ `processOrder_Success()` - Verifies successful order processing with payment and RabbitMQ messaging
- ✅ `processOrder_PaymentFailed()` - Tests handling of failed payments
- ✅ `processOrder_PaymentServiceUnavailable()` - Tests resilience when payment service is down
- ✅ `processOrder_RabbitMQFailure_OrderStillAccepted()` - Ensures orders are accepted even if messaging fails
- ✅ `processOrder_CalculatesCorrectAmount()` - Verifies price calculation (quantity × 15.99)

**Technologies Used:**
- JUnit 5
- Mockito
- Spring Test Utils (ReflectionTestUtils)

**Example Run:**
```bash
mvn test -Dtest=OrderServiceTest
```

#### OrderControllerTest

**Location:** `src/test/java/com/pizza/order/controller/OrderControllerTest.java`

**Purpose:** Tests the REST controller logic with mocked service layer.

**Key Test Cases:**
- ✅ `createOrder_Success()` - Validates HTTP 201 CREATED response
- ✅ `createOrder_PaymentFailed()` - Validates HTTP 402 PAYMENT_REQUIRED response
- ✅ `createOrder_Error()` - Validates HTTP 503 SERVICE_UNAVAILABLE response
- ✅ `health_ReturnsOk()` - Tests health check endpoint
- ✅ `handleGenericException_ReturnsInternalServerError()` - Tests exception handling

**Example Run:**
```bash
mvn test -Dtest=OrderControllerTest
```

#### OrderRequestTest

**Location:** `src/test/java/com/pizza/order/model/OrderRequestTest.java`

**Purpose:** Tests Jakarta Bean Validation constraints on the OrderRequest model.

**Key Test Cases:**
- ✅ `validOrderRequest()` - Validates correct request passes validation
- ✅ `invalidOrderRequest_NullPizza()` - Tests @NotBlank on pizza field
- ✅ `invalidOrderRequest_NullQuantity()` - Tests @NotNull on quantity
- ✅ `invalidOrderRequest_NegativeQuantity()` - Tests @Positive constraint
- ✅ `invalidOrderRequest_NullAddress()` - Tests address validation
- ✅ `invalidOrderRequest_NullCustomerName()` - Tests customer name validation
- ✅ `invalidOrderRequest_MultipleViolations()` - Tests multiple validation errors

**Example Run:**
```bash
mvn test -Dtest=OrderRequestTest
```

### 2. Integration Tests

Integration tests verify components working together with partial application context.

#### OrderControllerIntegrationTest

**Location:** `src/test/java/com/pizza/order/integration/OrderControllerIntegrationTest.java`

**Purpose:** Tests the REST API with MockMvc, including request/response serialization and validation.

**Key Test Cases:**
- ✅ `createOrder_ValidRequest_ReturnsCreated()` - Tests complete HTTP flow with valid data
- ✅ `createOrder_InvalidRequest_MissingPizza_ReturnsBadRequest()` - Tests validation error responses
- ✅ `createOrder_InvalidRequest_NegativeQuantity_ReturnsBadRequest()` - Tests quantity validation
- ✅ `health_ReturnsOk()` - Tests health endpoint availability

**Technologies Used:**
- Spring Boot Test
- MockMvc
- @WebMvcTest annotation
- Jackson ObjectMapper

**Example Run:**
```bash
mvn test -Dtest=OrderControllerIntegrationTest
```

#### RabbitMQIntegrationTest

**Location:** `src/test/java/com/pizza/order/integration/RabbitMQIntegrationTest.java`

**Purpose:** Tests RabbitMQ message publishing with Spring Boot context.

**Key Test Cases:**
- ✅ `processOrder_Success_SendsMessageToRabbitMQ()` - Verifies OrderPlacedEvent is sent
- ✅ `processOrder_PaymentFailed_DoesNotSendMessageToRabbitMQ()` - Ensures no message on payment failure
- ✅ `processOrder_MultipleOrders_SendsMultipleMessages()` - Tests multiple order processing
- ✅ `rabbitMQConfig_QueueExists()` - Validates queue configuration

**Technologies Used:**
- Spring Boot Test
- Spring AMQP Test
- @SpyBean for RabbitTemplate

**Example Run:**
```bash
mvn test -Dtest=RabbitMQIntegrationTest
```

### 3. API Tests (End-to-End)

API tests use the full Spring application context to test complete user scenarios.

#### OrderServiceApiTest

**Location:** `src/test/java/com/pizza/order/integration/OrderServiceApiTest.java`

**Purpose:** End-to-end API testing with complete application context.

**Key Test Cases:**
- ✅ `endToEnd_CreateOrder_Success()` - Full successful order flow
- ✅ `endToEnd_CreateOrder_PaymentDeclined()` - Complete payment failure scenario
- ✅ `endToEnd_CreateOrder_PaymentServiceDown()` - Service unavailability handling
- ✅ `endToEnd_MultipleOrders_DifferentQuantities()` - Tests various order quantities
- ✅ `endToEnd_ValidationErrors_AllFields()` - Tests comprehensive validation
- ✅ `endToEnd_PizzaTypes_VariousNames()` - Tests different pizza types
- ✅ `endToEnd_LongCustomerNames_AndAddresses()` - Tests edge cases with long strings
- ✅ `endToEnd_SpecialCharacters_InFields()` - Tests international characters
- ✅ `endToEnd_MaxQuantity()` - Tests large quantity orders

**Technologies Used:**
- Spring Boot Test
- @SpringBootTest annotation
- MockMvc with AutoConfigureMockMvc
- Full application context

**Example Run:**
```bash
mvn test -Dtest=OrderServiceApiTest
```

## Running Tests

### Run All Tests

```bash
cd order-service
mvn test
```

### Run Specific Test Category

```bash
# Unit tests only
mvn test -Dtest=*Test

# Integration tests only
mvn test -Dtest=*IntegrationTest

# API tests only
mvn test -Dtest=*ApiTest
```

### Run with Coverage Report

```bash
mvn test jacoco:report
```

The coverage report will be generated at: `target/site/jacoco/index.html`

### Run Specific Test Class

```bash
mvn test -Dtest=OrderServiceTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=OrderServiceTest#processOrder_Success
```

### Run Tests with Verbose Output

```bash
mvn test -X
```

## Test Configuration

### Test Properties

**File:** `src/test/resources/application-test.yml`

Key configurations for testing:
- Server runs on random port (port: 0)
- RabbitMQ connection settings (can be overridden with @MockBean)
- Payment service URL (mocked in tests)
- Debug logging for test visibility

### Active Profile

Tests use the `test` profile via `@ActiveProfiles("test")` annotation.

## Test Dependencies

The following test dependencies are configured in `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit-test</artifactId>
    <scope>test</scope>
</dependency>
```

These include:
- JUnit 5 (Jupiter)
- Mockito
- AssertJ
- Hamcrest
- Spring Test
- Spring AMQP Test

## Test Coverage

The test suite provides comprehensive coverage across:

1. **Business Logic**: Order processing, payment handling, messaging
2. **Validation**: Input validation with Jakarta Bean Validation
3. **Error Handling**: Payment failures, service unavailability, exceptions
4. **HTTP Layer**: Request/response mapping, status codes, JSON serialization
5. **Messaging**: RabbitMQ message publishing
6. **Edge Cases**: Long strings, special characters, boundary values

### Coverage Targets

- **Unit Tests**: ≥90% coverage for service and controller layers
- **Integration Tests**: All critical API paths and integrations
- **API Tests**: All user-facing scenarios and error paths

## Best Practices

1. **Test Isolation**: Each test is independent and can run in any order
2. **Clear Naming**: Test names clearly describe what is being tested
3. **AAA Pattern**: Tests follow Arrange-Act-Assert structure
4. **Minimal Mocking**: Only external dependencies (RestTemplate, RabbitMQ) are mocked
5. **Real Validation**: Uses actual Jakarta Validation instead of mocking
6. **Comprehensive Assertions**: Tests verify all relevant aspects of responses

## Common Test Scenarios

### Testing Successful Order

```java
OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Test Street", "John Doe");
PaymentResponse paymentResponse = new PaymentResponse("txn123", true, "Payment successful");
when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
    .thenReturn(paymentResponse);

mockMvc.perform(post("/orders")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(orderRequest)))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.status").value("SUCCESS"));
```

### Testing Validation Errors

```java
OrderRequest invalidRequest = new OrderRequest("", null, "", "");

mockMvc.perform(post("/orders")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(invalidRequest)))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.pizza").exists())
    .andExpect(jsonPath("$.quantity").exists());
```

### Testing Payment Failure

```java
PaymentResponse failedPayment = new PaymentResponse("txn", false, "Insufficient funds");
when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
    .thenReturn(failedPayment);

OrderResponse response = orderService.processOrder(orderRequest);

assertEquals("PAYMENT_FAILED", response.getStatus());
```

## Continuous Integration

These tests are designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Run tests
  run: mvn test
  
- name: Generate coverage report
  run: mvn jacoco:report
```

## Troubleshooting

### RabbitMQ Connection Issues in Tests

If tests fail due to RabbitMQ connection errors, ensure:
- RabbitMQ is mocked with `@MockBean` in integration tests
- Tests use `@ActiveProfiles("test")`

### Port Conflicts

If port 8080 is already in use:
- Tests automatically use random ports with `server.port: 0`

### Test Execution Order

Tests are independent and order-agnostic. If you see order-dependent failures:
- Check for shared mutable state
- Ensure proper `@BeforeEach` cleanup

## Future Enhancements

Potential additions to the test suite:

1. **Performance Tests**: Load testing with JMeter or Gatling
2. **Contract Tests**: Consumer-driven contracts with Pact
3. **Mutation Testing**: Using PIT for test quality assessment
4. **Security Tests**: Authentication/authorization if added
5. **Database Tests**: If persistence layer is added

## Conclusion

This comprehensive test suite ensures the Order Service is reliable, maintainable, and production-ready. The combination of unit, integration, and API tests provides confidence in the system's behavior across all layers.

For questions or issues with tests, please refer to the main project documentation or contact the development team.
