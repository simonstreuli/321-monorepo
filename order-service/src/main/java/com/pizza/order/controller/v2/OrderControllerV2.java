package com.pizza.order.controller.v2;

import com.pizza.models.OrderRequest;
import com.pizza.models.OrderResponse;
import com.pizza.order.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/orders")
public class OrderControllerV2 {

    private static final Logger logger = LoggerFactory.getLogger(OrderControllerV2.class);

    private final OrderService orderService;

    public OrderControllerV2(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        logger.info("V2 API - Received order request for {} x {} to {}", 
            orderRequest.getQuantity(), orderRequest.getPizza(), orderRequest.getAddress());
        
        OrderResponse response = orderService.processOrder(orderRequest);
        
        // V2 returns enhanced response with additional metadata
        Map<String, Object> enhancedResponse = new HashMap<>();
        enhancedResponse.put("orderId", response.getOrderId());
        enhancedResponse.put("status", response.getStatus());
        enhancedResponse.put("message", response.getMessage());
        enhancedResponse.put("apiVersion", "v2");
        enhancedResponse.put("timestamp", java.time.LocalDateTime.now().toString());
        
        HttpStatus status = switch (response.getStatus()) {
            case "SUCCESS" -> HttpStatus.CREATED;
            case "PAYMENT_FAILED" -> HttpStatus.PAYMENT_REQUIRED;
            case "ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        
        return ResponseEntity.status(status).body(enhancedResponse);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthResponse = new HashMap<>();
        healthResponse.put("status", "UP");
        healthResponse.put("service", "Order Service");
        healthResponse.put("version", "v2");
        healthResponse.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(healthResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errorResponse.put("status", "VALIDATION_ERROR");
        errorResponse.put("errors", fieldErrors);
        errorResponse.put("apiVersion", "v2");
        errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error in V2 API: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", "An unexpected error occurred. Please try again later.");
        errorResponse.put("apiVersion", "v2");
        errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
