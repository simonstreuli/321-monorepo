package com.pizza.payment.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestTest {

    private final Validator validator;

    public PaymentRequestTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("All fields valid - no constraint violations")
    void testValidPaymentRequest() {
        PaymentRequest request = new PaymentRequest("order123", "John Doe", 50.0);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Null orderId - constraint violation")
    void testNullOrderId() {
        PaymentRequest request = new PaymentRequest(null, "John Doe", 50.0);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orderId")));
    }

    @Test
    @DisplayName("Blank orderId - constraint violation")
    void testBlankOrderId() {
        PaymentRequest request = new PaymentRequest("   ", "John Doe", 50.0);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orderId")));
    }

    @Test
    @DisplayName("Null customerName - constraint violation")
    void testNullCustomerName() {
        PaymentRequest request = new PaymentRequest("order123", null, 50.0);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerName")));
    }

    @Test
    @DisplayName("Blank customerName - constraint violation")
    void testBlankCustomerName() {
        PaymentRequest request = new PaymentRequest("order123", "   ", 50.0);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerName")));
    }

    @Test
    @DisplayName("Zero amount - constraint violation")
    void testZeroAmount() {
        PaymentRequest request = new PaymentRequest("order123", "John Doe", 0.0);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    @DisplayName("Negative amount - constraint violation")
    void testNegativeAmount() {
        PaymentRequest request = new PaymentRequest("order123", "John Doe", -10.0);
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    @DisplayName("NoArgsConstructor and setters")
    void testNoArgsConstructorAndSetters() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("order456");
        request.setCustomerName("Jane Doe");
        request.setAmount(25.0);
        assertEquals("order456", request.getOrderId());
        assertEquals("Jane Doe", request.getCustomerName());
        assertEquals(25.0, request.getAmount());
    }

    @Test
    @DisplayName("AllArgsConstructor and getters")
    void testAllArgsConstructorAndGetters() {
        PaymentRequest request = new PaymentRequest("order789", "Alice", 100.0);
        assertEquals("order789", request.getOrderId());
        assertEquals("Alice", request.getCustomerName());
        assertEquals(100.0, request.getAmount());
    }

    @Test
    @DisplayName("Equals and hashCode")
    void testEqualsAndHashCode() {
        PaymentRequest req1 = new PaymentRequest("order1", "Bob", 10.0);
        PaymentRequest req2 = new PaymentRequest("order1", "Bob", 10.0);
        PaymentRequest req3 = new PaymentRequest("order2", "Bob", 10.0);
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertNotEquals(req1, req3);
    }

    @Test
    @DisplayName("toString method")
    void testToString() {
        PaymentRequest request = new PaymentRequest("order999", "Eve", 75.0);
        String str = request.toString();
        assertTrue(str.contains("order999"));
        assertTrue(str.contains("Eve"));
        assertTrue(str.contains("75.0"));
    }
}