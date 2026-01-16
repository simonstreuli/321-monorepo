package com.pizza.order.controller;

import com.pizza.order.model.OrderRequest;
import com.pizza.order.model.OrderResponse;
import com.pizza.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(orderService);
    }

    @Test
    void createOrder_Success() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Test Street 1", "John Doe");
        OrderResponse orderResponse = new OrderResponse("order123", "SUCCESS", "Order placed successfully");
        
        when(orderService.processOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // When
        ResponseEntity<OrderResponse> response = orderController.createOrder(orderRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getStatus());
        assertEquals("order123", response.getBody().getOrderId());
        
        verify(orderService).processOrder(orderRequest);
    }

    @Test
    void createOrder_PaymentFailed() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Pepperoni", 1, "Test Street 2", "Jane Doe");
        OrderResponse orderResponse = new OrderResponse("order456", "PAYMENT_FAILED", "Payment declined");
        
        when(orderService.processOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // When
        ResponseEntity<OrderResponse> response = orderController.createOrder(orderRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.PAYMENT_REQUIRED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PAYMENT_FAILED", response.getBody().getStatus());
        
        verify(orderService).processOrder(orderRequest);
    }

    @Test
    void createOrder_Error() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Hawaiian", 3, "Test Street 3", "Bob Smith");
        OrderResponse orderResponse = new OrderResponse("order789", "ERROR", "Service unavailable");
        
        when(orderService.processOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // When
        ResponseEntity<OrderResponse> response = orderController.createOrder(orderRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        
        verify(orderService).processOrder(orderRequest);
    }

    @Test
    void createOrder_UnknownStatus() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Quattro Formaggi", 1, "Test Street 4", "Alice");
        OrderResponse orderResponse = new OrderResponse("order999", "UNKNOWN", "Unknown status");
        
        when(orderService.processOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // When
        ResponseEntity<OrderResponse> response = orderController.createOrder(orderRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        verify(orderService).processOrder(orderRequest);
    }

    @Test
    void health_ReturnsOk() {
        // When
        ResponseEntity<String> response = orderController.health();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order Service is running", response.getBody());
        
        verifyNoInteractions(orderService);
    }

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<OrderResponse> response = orderController.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
        assertNull(response.getBody().getOrderId());
    }
}
