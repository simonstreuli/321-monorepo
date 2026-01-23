package com.pizza.delivery.controller;

import com.pizza.delivery.model.DeliveryStatus;
import com.pizza.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/deliveries")
@Tag(name = "Deliveries", description = "Delivery management and tracking API")
public class DeliveryController {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryController.class);

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Operation(summary = "Get delivery status by order ID",
            description = "Retrieves the current delivery status for a specific order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery status found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeliveryStatus.class))),
            @ApiResponse(responseCode = "404", description = "Delivery not found",
                    content = @Content)
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<DeliveryStatus> getDeliveryStatus(
            @Parameter(description = "Order ID (UUID format)", required = true)
            @PathVariable String orderId) {
        logger.info("Checking delivery status for order {}", orderId);

        DeliveryStatus status = deliveryService.getDeliveryStatus(orderId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Get all deliveries",
            description = "Retrieves all active deliveries as a map of order IDs to delivery statuses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all deliveries",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<Map<String, DeliveryStatus>> getAllDeliveries() {
        logger.info("Fetching all deliveries");
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @Operation(summary = "Health check",
            description = "Returns a simple health check message to verify the service is running")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy",
                    content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Delivery Service is running");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body("An unexpected error occurred.");
    }
}
