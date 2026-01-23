package com.pizza.delivery.service;

import com.pizza.models.DeliveryStatus;
import com.pizza.models.OrderReadyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeliveryService Unit Tests")
class DeliveryServiceTest {

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService();
    }

    @Test
    @DisplayName("Should create delivery with ASSIGNED status when order ready event is received")
    void handleOrderReady_shouldCreateDeliveryWithAssignedStatus() {
        // Given
        OrderReadyEvent event = new OrderReadyEvent(
                "order-123",
                "Margherita",
                2,
                "Musterstrasse 123, 8000 Zurich",
                "Max Mustermann",
                LocalDateTime.now()
        );

        // When
        deliveryService.handleOrderReady(event);

        // Then
        DeliveryStatus status = deliveryService.getDeliveryStatus("order-123");
        assertNotNull(status);
        assertEquals("order-123", status.getOrderId());
        assertEquals("ASSIGNED", status.getStatus());
        assertEquals("Musterstrasse 123, 8000 Zurich", status.getAddress());
        assertNotNull(status.getDriverName());
        assertNotNull(status.getAssignedAt());
        assertNotNull(status.getEstimatedDeliveryTime());
        assertNotNull(status.getTargetInTransitTime());
        assertNull(status.getDeliveredAt());
        assertNull(status.getInTransitAt());
    }

    @Test
    @DisplayName("Should assign driver from pool when order ready event is received")
    void handleOrderReady_shouldAssignDriverFromPool() {
        // Given
        OrderReadyEvent event = new OrderReadyEvent(
                "order-456",
                "Pepperoni",
                1,
                "Bahnhofstrasse 45, 8001 Zurich",
                "Anna Schmidt",
                LocalDateTime.now()
        );

        // When
        deliveryService.handleOrderReady(event);

        // Then
        DeliveryStatus status = deliveryService.getDeliveryStatus("order-456");
        assertNotNull(status.getDriverName());
        assertFalse(status.getDriverName().isEmpty());
    }

    @Test
    @DisplayName("Should return null when delivery does not exist")
    void getDeliveryStatus_shouldReturnNullWhenNotFound() {
        // When
        DeliveryStatus status = deliveryService.getDeliveryStatus("non-existent-order");

        // Then
        assertNull(status);
    }

    @Test
    @DisplayName("Should return all deliveries")
    void getAllDeliveries_shouldReturnAllDeliveries() {
        // Given
        OrderReadyEvent event1 = new OrderReadyEvent(
                "order-1",
                "Margherita",
                1,
                "Address 1",
                "Customer 1",
                LocalDateTime.now()
        );
        OrderReadyEvent event2 = new OrderReadyEvent(
                "order-2",
                "Pepperoni",
                2,
                "Address 2",
                "Customer 2",
                LocalDateTime.now()
        );

        deliveryService.handleOrderReady(event1);
        deliveryService.handleOrderReady(event2);

        // When
        Map<String, DeliveryStatus> deliveries = deliveryService.getAllDeliveries();

        // Then
        assertEquals(2, deliveries.size());
        assertTrue(deliveries.containsKey("order-1"));
        assertTrue(deliveries.containsKey("order-2"));
    }

    @Test
    @DisplayName("Should return empty map when no deliveries exist")
    void getAllDeliveries_shouldReturnEmptyMapWhenNoDeliveries() {
        // When
        Map<String, DeliveryStatus> deliveries = deliveryService.getAllDeliveries();

        // Then
        assertNotNull(deliveries);
        assertTrue(deliveries.isEmpty());
    }

    @Test
    @DisplayName("Should set estimated delivery time in the future")
    void handleOrderReady_shouldSetEstimatedDeliveryTimeInFuture() {
        // Given
        LocalDateTime beforeTest = LocalDateTime.now();
        OrderReadyEvent event = new OrderReadyEvent(
                "order-789",
                "Hawaiian",
                1,
                "Test Address",
                "Test Customer",
                LocalDateTime.now()
        );

        // When
        deliveryService.handleOrderReady(event);

        // Then
        DeliveryStatus status = deliveryService.getDeliveryStatus("order-789");
        assertTrue(status.getEstimatedDeliveryTime().isAfter(beforeTest));
    }

    @Test
    @DisplayName("Should set target in transit time after assigned time")
    void handleOrderReady_shouldSetTargetInTransitTimeAfterAssigned() {
        // Given
        OrderReadyEvent event = new OrderReadyEvent(
                "order-timing",
                "Quattro Formaggi",
                1,
                "Test Address",
                "Test Customer",
                LocalDateTime.now()
        );

        // When
        deliveryService.handleOrderReady(event);

        // Then
        DeliveryStatus status = deliveryService.getDeliveryStatus("order-timing");
        assertTrue(status.getTargetInTransitTime().isAfter(status.getAssignedAt()) ||
                status.getTargetInTransitTime().isEqual(status.getAssignedAt()));
    }

    @Test
    @DisplayName("Should handle multiple orders correctly")
    void handleOrderReady_shouldHandleMultipleOrdersCorrectly() {
        // Given
        for (int i = 0; i < 10; i++) {
            OrderReadyEvent event = new OrderReadyEvent(
                    "order-multi-" + i,
                    "Pizza " + i,
                    i + 1,
                    "Address " + i,
                    "Customer " + i,
                    LocalDateTime.now()
            );
            deliveryService.handleOrderReady(event);
        }

        // Then
        Map<String, DeliveryStatus> deliveries = deliveryService.getAllDeliveries();
        assertEquals(10, deliveries.size());

        for (int i = 0; i < 10; i++) {
            DeliveryStatus status = deliveryService.getDeliveryStatus("order-multi-" + i);
            assertNotNull(status);
            assertEquals("ASSIGNED", status.getStatus());
        }
    }

    @Test
    @DisplayName("Should update delivery status from ASSIGNED to IN_TRANSIT when target time is reached")
    void updateDeliveryStatuses_shouldTransitionToInTransit() throws InterruptedException {
        // Given
        OrderReadyEvent event = new OrderReadyEvent(
                "order-transit",
                "Margherita",
                1,
                "Test Address",
                "Test Customer",
                LocalDateTime.now()
        );
        deliveryService.handleOrderReady(event);

        DeliveryStatus status = deliveryService.getDeliveryStatus("order-transit");
        // Set target in transit time to past to trigger transition
        status.setTargetInTransitTime(LocalDateTime.now().minusSeconds(1));

        // When
        deliveryService.updateDeliveryStatuses();

        // Then
        DeliveryStatus updatedStatus = deliveryService.getDeliveryStatus("order-transit");
        assertEquals("IN_TRANSIT", updatedStatus.getStatus());
        assertNotNull(updatedStatus.getInTransitAt());
        assertNotNull(updatedStatus.getTargetDeliveredTime());
    }

    @Test
    @DisplayName("Should update delivery status from IN_TRANSIT to DELIVERED when target time is reached")
    void updateDeliveryStatuses_shouldTransitionToDelivered() {
        // Given
        OrderReadyEvent event = new OrderReadyEvent(
                "order-delivered",
                "Pepperoni",
                1,
                "Test Address",
                "Test Customer",
                LocalDateTime.now()
        );
        deliveryService.handleOrderReady(event);

        DeliveryStatus status = deliveryService.getDeliveryStatus("order-delivered");
        // First transition to IN_TRANSIT
        status.setTargetInTransitTime(LocalDateTime.now().minusSeconds(1));
        deliveryService.updateDeliveryStatuses();

        // Set target delivered time to past to trigger delivery
        status = deliveryService.getDeliveryStatus("order-delivered");
        status.setTargetDeliveredTime(LocalDateTime.now().minusSeconds(1));

        // When
        deliveryService.updateDeliveryStatuses();

        // Then
        DeliveryStatus updatedStatus = deliveryService.getDeliveryStatus("order-delivered");
        assertEquals("DELIVERED", updatedStatus.getStatus());
        assertNotNull(updatedStatus.getDeliveredAt());
    }

    @Test
    @DisplayName("Should not transition status when target time is not reached")
    void updateDeliveryStatuses_shouldNotTransitionWhenTargetTimeNotReached() {
        // Given
        OrderReadyEvent event = new OrderReadyEvent(
                "order-not-ready",
                "Margherita",
                1,
                "Test Address",
                "Test Customer",
                LocalDateTime.now()
        );
        deliveryService.handleOrderReady(event);

        DeliveryStatus status = deliveryService.getDeliveryStatus("order-not-ready");
        // Set target in transit time to future
        status.setTargetInTransitTime(LocalDateTime.now().plusHours(1));

        // When
        deliveryService.updateDeliveryStatuses();

        // Then
        DeliveryStatus updatedStatus = deliveryService.getDeliveryStatus("order-not-ready");
        assertEquals("ASSIGNED", updatedStatus.getStatus());
        assertNull(updatedStatus.getInTransitAt());
    }
}
