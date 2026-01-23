package com.pizza.delivery.model;

import com.pizza.models.OrderReadyEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderReadyEvent Model Tests")
class OrderReadyEventTest {

    @Test
    @DisplayName("Should create OrderReadyEvent with all arguments constructor")
    void shouldCreateOrderReadyEventWithAllArgsConstructor() {
        // Given
        String orderId = "order-123";
        String pizza = "Margherita";
        Integer quantity = 2;
        String address = "Musterstrasse 123, 8000 Zurich";
        String customerName = "Max Mustermann";
        LocalDateTime preparedAt = LocalDateTime.now();

        // When
        OrderReadyEvent event = new OrderReadyEvent(
                orderId, pizza, quantity, address, customerName, preparedAt
        );

        // Then
        assertEquals(orderId, event.getOrderId());
        assertEquals(pizza, event.getPizza());
        assertEquals(quantity, event.getQuantity());
        assertEquals(address, event.getAddress());
        assertEquals(customerName, event.getCustomerName());
        assertEquals(preparedAt, event.getPreparedAt());
    }

    @Test
    @DisplayName("Should create OrderReadyEvent with no-args constructor and setters")
    void shouldCreateOrderReadyEventWithNoArgsConstructorAndSetters() {
        // Given
        OrderReadyEvent event = new OrderReadyEvent();
        String orderId = "order-456";
        String pizza = "Pepperoni";
        Integer quantity = 3;
        String address = "Bahnhofstrasse 45, 8001 Zurich";
        String customerName = "Anna Schmidt";
        LocalDateTime preparedAt = LocalDateTime.now();

        // When
        event.setOrderId(orderId);
        event.setPizza(pizza);
        event.setQuantity(quantity);
        event.setAddress(address);
        event.setCustomerName(customerName);
        event.setPreparedAt(preparedAt);

        // Then
        assertEquals(orderId, event.getOrderId());
        assertEquals(pizza, event.getPizza());
        assertEquals(quantity, event.getQuantity());
        assertEquals(address, event.getAddress());
        assertEquals(customerName, event.getCustomerName());
        assertEquals(preparedAt, event.getPreparedAt());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        OrderReadyEvent event1 = new OrderReadyEvent(
                "order-1", "Margherita", 2, "Address", "Customer", now
        );
        OrderReadyEvent event2 = new OrderReadyEvent(
                "order-1", "Margherita", 2, "Address", "Customer", now
        );
        OrderReadyEvent event3 = new OrderReadyEvent(
                "order-2", "Pepperoni", 1, "Different Address", "Different Customer", now
        );

        // Then
        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        OrderReadyEvent event1 = new OrderReadyEvent(
                "order-1", "Margherita", 2, "Address", "Customer", now
        );
        OrderReadyEvent event2 = new OrderReadyEvent(
                "order-1", "Margherita", 2, "Address", "Customer", now
        );

        // Then
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        OrderReadyEvent event = new OrderReadyEvent(
                "order-1", "Margherita", 2, "Address", "Customer",
                LocalDateTime.of(2026, 1, 16, 10, 0)
        );

        // When
        String toString = event.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("order-1"));
        assertTrue(toString.contains("Margherita"));
        assertTrue(toString.contains("2"));
        assertTrue(toString.contains("Customer"));
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        // Given
        OrderReadyEvent event = new OrderReadyEvent();

        // Then
        assertNull(event.getOrderId());
        assertNull(event.getPizza());
        assertNull(event.getQuantity());
        assertNull(event.getAddress());
        assertNull(event.getCustomerName());
        assertNull(event.getPreparedAt());
    }

    @Test
    @DisplayName("Should handle different pizza types")
    void shouldHandleDifferentPizzaTypes() {
        // Given
        String[] pizzaTypes = {"Margherita", "Pepperoni", "Hawaiian", "Quattro Formaggi", "Vegetarian"};
        LocalDateTime now = LocalDateTime.now();

        for (String pizzaType : pizzaTypes) {
            // When
            OrderReadyEvent event = new OrderReadyEvent(
                    "order-" + pizzaType, pizzaType, 1, "Address", "Customer", now
            );

            // Then
            assertEquals(pizzaType, event.getPizza());
        }
    }
}
