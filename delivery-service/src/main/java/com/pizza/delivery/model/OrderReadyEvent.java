package com.pizza.delivery.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Event received when an order is ready for delivery")
public class OrderReadyEvent {
    @Schema(description = "Unique order identifier (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    private String orderId;

    @Schema(description = "Type of pizza ordered", example = "Margherita")
    private String pizza;

    @Schema(description = "Number of pizzas ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Delivery address", example = "Musterstrasse 123, 8000 Zurich")
    private String address;

    @Schema(description = "Name of the customer", example = "Max Mustermann")
    private String customerName;

    @Schema(description = "Timestamp when the order was prepared", example = "2026-01-16T10:00:00")
    private LocalDateTime preparedAt;
}
