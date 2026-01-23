package com.pizza.delivery.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Delivery status information for an order")
public class DeliveryStatus {
    @Schema(description = "Unique order identifier (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    private String orderId;

    @Schema(description = "Current delivery status", example = "IN_TRANSIT", allowableValues = {"ASSIGNED", "IN_TRANSIT", "DELIVERED"})
    private String status;

    @Schema(description = "Name of the assigned driver", example = "Max Mustermann")
    private String driverName;

    @Schema(description = "Delivery address", example = "Musterstrasse 123, 8000 Zurich")
    private String address;

    @Schema(description = "Timestamp when the driver was assigned", example = "2026-01-16T10:00:00")
    private LocalDateTime assignedAt;

    @Schema(description = "Estimated delivery time", example = "2026-01-16T10:30:00")
    private LocalDateTime estimatedDeliveryTime;

    @Schema(description = "Actual delivery timestamp (null if not yet delivered)", example = "2026-01-16T10:25:00")
    private LocalDateTime deliveredAt;

    @Schema(description = "Timestamp when the driver started transit", example = "2026-01-16T10:05:00")
    private LocalDateTime inTransitAt;

    @Schema(description = "Target time for status change to IN_TRANSIT (internal scheduling)")
    private LocalDateTime targetInTransitTime;

    @Schema(description = "Target time for status change to DELIVERED (internal scheduling)")
    private LocalDateTime targetDeliveredTime;
}
