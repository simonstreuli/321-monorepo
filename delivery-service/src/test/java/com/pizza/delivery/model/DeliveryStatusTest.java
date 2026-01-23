package com.pizza.delivery.model;

import com.pizza.models.DeliveryStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeliveryStatus Model Tests")
class DeliveryStatusTest {

    @Test
    @DisplayName("Should create DeliveryStatus with all arguments constructor")
    void shouldCreateDeliveryStatusWithAllArgsConstructor() {
        // Given
        String orderId = "order-123";
        String status = "ASSIGNED";
        String driverName = "Max Mustermann";
        String address = "Musterstrasse 123, 8000 Zurich";
        LocalDateTime assignedAt = LocalDateTime.now();
        LocalDateTime estimatedDeliveryTime = assignedAt.plusMinutes(30);
        LocalDateTime deliveredAt = null;
        LocalDateTime inTransitAt = null;
        LocalDateTime targetInTransitTime = assignedAt.plusSeconds(12);
        LocalDateTime targetDeliveredTime = null;

        // When
        DeliveryStatus deliveryStatus = new DeliveryStatus(
                orderId, status, driverName, address, assignedAt,
                estimatedDeliveryTime, deliveredAt, inTransitAt,
                targetInTransitTime, targetDeliveredTime
        );

        // Then
        assertEquals(orderId, deliveryStatus.getOrderId());
        assertEquals(status, deliveryStatus.getStatus());
        assertEquals(driverName, deliveryStatus.getDriverName());
        assertEquals(address, deliveryStatus.getAddress());
        assertEquals(assignedAt, deliveryStatus.getAssignedAt());
        assertEquals(estimatedDeliveryTime, deliveryStatus.getEstimatedDeliveryTime());
        assertNull(deliveryStatus.getDeliveredAt());
        assertNull(deliveryStatus.getInTransitAt());
        assertEquals(targetInTransitTime, deliveryStatus.getTargetInTransitTime());
        assertNull(deliveryStatus.getTargetDeliveredTime());
    }

    @Test
    @DisplayName("Should create DeliveryStatus with no-args constructor and setters")
    void shouldCreateDeliveryStatusWithNoArgsConstructorAndSetters() {
        // Given
        DeliveryStatus deliveryStatus = new DeliveryStatus();
        String orderId = "order-456";
        String status = "IN_TRANSIT";
        String driverName = "Anna Schmidt";
        String address = "Bahnhofstrasse 45, 8001 Zurich";
        LocalDateTime assignedAt = LocalDateTime.now();
        LocalDateTime inTransitAt = assignedAt.plusSeconds(12);

        // When
        deliveryStatus.setOrderId(orderId);
        deliveryStatus.setStatus(status);
        deliveryStatus.setDriverName(driverName);
        deliveryStatus.setAddress(address);
        deliveryStatus.setAssignedAt(assignedAt);
        deliveryStatus.setInTransitAt(inTransitAt);

        // Then
        assertEquals(orderId, deliveryStatus.getOrderId());
        assertEquals(status, deliveryStatus.getStatus());
        assertEquals(driverName, deliveryStatus.getDriverName());
        assertEquals(address, deliveryStatus.getAddress());
        assertEquals(assignedAt, deliveryStatus.getAssignedAt());
        assertEquals(inTransitAt, deliveryStatus.getInTransitAt());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        DeliveryStatus status1 = new DeliveryStatus(
                "order-1", "ASSIGNED", "Driver", "Address",
                now, now.plusMinutes(30), null, null, now.plusSeconds(12), null
        );
        DeliveryStatus status2 = new DeliveryStatus(
                "order-1", "ASSIGNED", "Driver", "Address",
                now, now.plusMinutes(30), null, null, now.plusSeconds(12), null
        );
        DeliveryStatus status3 = new DeliveryStatus(
                "order-2", "ASSIGNED", "Driver", "Address",
                now, now.plusMinutes(30), null, null, now.plusSeconds(12), null
        );

        // Then
        assertEquals(status1, status2);
        assertNotEquals(status1, status3);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        DeliveryStatus status1 = new DeliveryStatus(
                "order-1", "ASSIGNED", "Driver", "Address",
                now, now.plusMinutes(30), null, null, now.plusSeconds(12), null
        );
        DeliveryStatus status2 = new DeliveryStatus(
                "order-1", "ASSIGNED", "Driver", "Address",
                now, now.plusMinutes(30), null, null, now.plusSeconds(12), null
        );

        // Then
        assertEquals(status1.hashCode(), status2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        DeliveryStatus status = new DeliveryStatus(
                "order-1", "ASSIGNED", "Driver", "Address",
                LocalDateTime.of(2026, 1, 16, 10, 0),
                LocalDateTime.of(2026, 1, 16, 10, 30),
                null, null, null, null
        );

        // When
        String toString = status.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("order-1"));
        assertTrue(toString.contains("ASSIGNED"));
        assertTrue(toString.contains("Driver"));
    }

    @Test
    @DisplayName("Should update status correctly through lifecycle")
    void shouldUpdateStatusThroughLifecycle() {
        // Given
        DeliveryStatus status = new DeliveryStatus();
        status.setOrderId("order-lifecycle");
        status.setStatus("ASSIGNED");
        status.setDriverName("Test Driver");
        status.setAddress("Test Address");
        LocalDateTime now = LocalDateTime.now();
        status.setAssignedAt(now);
        status.setTargetInTransitTime(now.plusSeconds(12));

        // Verify initial state
        assertEquals("ASSIGNED", status.getStatus());
        assertNull(status.getInTransitAt());

        // Transition to IN_TRANSIT
        status.setStatus("IN_TRANSIT");
        status.setInTransitAt(now.plusSeconds(12));
        status.setTargetDeliveredTime(now.plusSeconds(30));

        assertEquals("IN_TRANSIT", status.getStatus());
        assertNotNull(status.getInTransitAt());

        // Transition to DELIVERED
        status.setStatus("DELIVERED");
        status.setDeliveredAt(now.plusSeconds(30));

        assertEquals("DELIVERED", status.getStatus());
        assertNotNull(status.getDeliveredAt());
    }
}
