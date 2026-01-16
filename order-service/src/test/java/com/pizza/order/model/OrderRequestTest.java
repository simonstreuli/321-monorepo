package com.pizza.order.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validOrderRequest() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Test Street 1", "John Doe");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidOrderRequest_NullPizza() {
        // Given
        OrderRequest orderRequest = new OrderRequest(null, 2, "Test Street 1", "John Doe");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Pizza type is required"));
    }

    @Test
    void invalidOrderRequest_BlankPizza() {
        // Given
        OrderRequest orderRequest = new OrderRequest("", 2, "Test Street 1", "John Doe");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.iterator().next().getMessage().contains("Pizza type is required"));
    }

    @Test
    void invalidOrderRequest_NullQuantity() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", null, "Test Street 1", "John Doe");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Quantity is required"));
    }

    @Test
    void invalidOrderRequest_NegativeQuantity() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", -1, "Test Street 1", "John Doe");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.iterator().next().getMessage().contains("Quantity must be positive"));
    }

    @Test
    void invalidOrderRequest_ZeroQuantity() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 0, "Test Street 1", "John Doe");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.iterator().next().getMessage().contains("Quantity must be positive"));
    }

    @Test
    void invalidOrderRequest_NullAddress() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, null, "John Doe");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.iterator().next().getMessage().contains("Delivery address is required"));
    }

    @Test
    void invalidOrderRequest_BlankAddress() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "   ", "John Doe");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.iterator().next().getMessage().contains("Delivery address is required"));
    }

    @Test
    void invalidOrderRequest_NullCustomerName() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Test Street 1", null);

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.iterator().next().getMessage().contains("Customer name is required"));
    }

    @Test
    void invalidOrderRequest_BlankCustomerName() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Test Street 1", "");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.iterator().next().getMessage().contains("Customer name is required"));
    }

    @Test
    void invalidOrderRequest_MultipleViolations() {
        // Given
        OrderRequest orderRequest = new OrderRequest("", null, "", "");

        // When
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(orderRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(4, violations.size());
    }

    @Test
    void orderRequest_GettersAndSetters() {
        // Given
        OrderRequest orderRequest = new OrderRequest();

        // When
        orderRequest.setPizza("Pepperoni");
        orderRequest.setQuantity(3);
        orderRequest.setAddress("Main Street 5");
        orderRequest.setCustomerName("Jane Doe");

        // Then
        assertEquals("Pepperoni", orderRequest.getPizza());
        assertEquals(3, orderRequest.getQuantity());
        assertEquals("Main Street 5", orderRequest.getAddress());
        assertEquals("Jane Doe", orderRequest.getCustomerName());
    }
}
