package com.pizza.delivery.controller;

import com.pizza.delivery.model.DeliveryStatus;
import com.pizza.delivery.service.DeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeliveryController.class)
@DisplayName("DeliveryController Unit Tests")
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @Test
    @DisplayName("Should return delivery status when order exists")
    void getDeliveryStatus_shouldReturnStatus() throws Exception {
        // Given
        String orderId = "order-123";
        DeliveryStatus status = new DeliveryStatus(
                orderId,
                "IN_TRANSIT",
                "Max Mustermann",
                "Musterstrasse 123, 8000 Zurich",
                LocalDateTime.of(2026, 1, 16, 10, 0),
                LocalDateTime.of(2026, 1, 16, 10, 30),
                null,
                LocalDateTime.of(2026, 1, 16, 10, 5),
                LocalDateTime.of(2026, 1, 16, 10, 5),
                LocalDateTime.of(2026, 1, 16, 10, 20)
        );
        when(deliveryService.getDeliveryStatus(orderId)).thenReturn(status);

        // When & Then
        mockMvc.perform(get("/api/v1/deliveries/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.driverName").value("Max Mustermann"))
                .andExpect(jsonPath("$.address").value("Musterstrasse 123, 8000 Zurich"));
    }

    @Test
    @DisplayName("Should return 404 when delivery does not exist")
    void getDeliveryStatus_shouldReturn404WhenNotFound() throws Exception {
        // Given
        when(deliveryService.getDeliveryStatus(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/deliveries/{orderId}", "non-existent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return all deliveries")
    void getAllDeliveries_shouldReturnAllDeliveries() throws Exception {
        // Given
        Map<String, DeliveryStatus> deliveries = new HashMap<>();
        deliveries.put("order-1", new DeliveryStatus(
                "order-1",
                "ASSIGNED",
                "Driver 1",
                "Address 1",
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30),
                null,
                null,
                LocalDateTime.now().plusSeconds(15),
                null
        ));
        deliveries.put("order-2", new DeliveryStatus(
                "order-2",
                "DELIVERED",
                "Driver 2",
                "Address 2",
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now(),
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().minusMinutes(20),
                LocalDateTime.now().minusMinutes(20),
                LocalDateTime.now().minusMinutes(5)
        ));
        when(deliveryService.getAllDeliveries()).thenReturn(deliveries);

        // When & Then
        mockMvc.perform(get("/api/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.order-1").exists())
                .andExpect(jsonPath("$.order-2").exists())
                .andExpect(jsonPath("$.order-1.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.order-2.status").value("DELIVERED"));
    }

    @Test
    @DisplayName("Should return empty map when no deliveries exist")
    void getAllDeliveries_shouldReturnEmptyMap() throws Exception {
        // Given
        when(deliveryService.getAllDeliveries()).thenReturn(new HashMap<>());

        // When & Then
        mockMvc.perform(get("/api/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{}"));
    }

    @Test
    @DisplayName("Should return health check message")
    void health_shouldReturnHealthMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/deliveries/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Delivery Service is running"));
    }

    @Test
    @DisplayName("Should return delivery with ASSIGNED status")
    void getDeliveryStatus_shouldReturnAssignedStatus() throws Exception {
        // Given
        String orderId = "order-assigned";
        DeliveryStatus status = new DeliveryStatus(
                orderId,
                "ASSIGNED",
                "Anna Schmidt",
                "Bahnhofstrasse 45, 8001 Zurich",
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(25),
                null,
                null,
                LocalDateTime.now().plusSeconds(12),
                null
        );
        when(deliveryService.getDeliveryStatus(orderId)).thenReturn(status);

        // When & Then
        mockMvc.perform(get("/api/v1/deliveries/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.inTransitAt").doesNotExist())
                .andExpect(jsonPath("$.deliveredAt").doesNotExist());
    }

    @Test
    @DisplayName("Should return delivery with DELIVERED status")
    void getDeliveryStatus_shouldReturnDeliveredStatus() throws Exception {
        // Given
        String orderId = "order-delivered";
        LocalDateTime deliveredTime = LocalDateTime.of(2026, 1, 16, 10, 25);
        DeliveryStatus status = new DeliveryStatus(
                orderId,
                "DELIVERED",
                "Peter Mueller",
                "Seestrasse 77, 8002 Zurich",
                LocalDateTime.of(2026, 1, 16, 10, 0),
                LocalDateTime.of(2026, 1, 16, 10, 30),
                deliveredTime,
                LocalDateTime.of(2026, 1, 16, 10, 10),
                LocalDateTime.of(2026, 1, 16, 10, 10),
                LocalDateTime.of(2026, 1, 16, 10, 25)
        );
        when(deliveryService.getDeliveryStatus(orderId)).thenReturn(status);

        // When & Then
        mockMvc.perform(get("/api/v1/deliveries/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.deliveredAt").exists());
    }
}
