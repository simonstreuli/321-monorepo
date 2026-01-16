package com.pizza.order.service;

import com.pizza.order.config.RabbitMQConfig;
import com.pizza.order.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private OrderService orderService;

    private static final String PAYMENT_SERVICE_URL = "http://localhost:8081";
    private static final double PIZZA_PRICE = 15.99;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(restTemplate, rabbitTemplate);
        ReflectionTestUtils.setField(orderService, "paymentServiceUrl", PAYMENT_SERVICE_URL);
    }

    @Test
    void processOrder_Success() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Test Street 1", "John Doe");
        PaymentResponse paymentResponse = new PaymentResponse("txn123", true, "Payment successful");
        
        when(restTemplate.postForObject(anyString(), any(PaymentRequest.class), eq(PaymentResponse.class)))
            .thenReturn(paymentResponse);

        // When
        OrderResponse response = orderService.processOrder(orderRequest);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.getMessage().contains("Order placed successfully"));
        assertNotNull(response.getOrderId());

        // Verify payment was called with correct amount
        ArgumentCaptor<PaymentRequest> paymentCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(restTemplate).postForObject(
            eq(PAYMENT_SERVICE_URL + "/pay"),
            paymentCaptor.capture(),
            eq(PaymentResponse.class)
        );
        PaymentRequest capturedPayment = paymentCaptor.getValue();
        assertEquals(2 * PIZZA_PRICE, capturedPayment.getAmount()); // 2 * 15.99
        assertEquals("John Doe", capturedPayment.getCustomerName());

        // Verify message was sent to RabbitMQ
        ArgumentCaptor<OrderPlacedEvent> eventCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.ORDER_PLACED_QUEUE),
            eventCaptor.capture()
        );
        OrderPlacedEvent capturedEvent = eventCaptor.getValue();
        assertEquals("Margherita", capturedEvent.getPizza());
        assertEquals(2, capturedEvent.getQuantity());
        assertEquals("Test Street 1", capturedEvent.getAddress());
        assertEquals("John Doe", capturedEvent.getCustomerName());
        assertNotNull(capturedEvent.getTimestamp());
    }

    @Test
    void processOrder_PaymentFailed() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Pepperoni", 1, "Test Street 2", "Jane Doe");
        PaymentResponse paymentResponse = new PaymentResponse("txn456", false, "Insufficient funds");
        
        when(restTemplate.postForObject(anyString(), any(PaymentRequest.class), eq(PaymentResponse.class)))
            .thenReturn(paymentResponse);

        // When
        OrderResponse response = orderService.processOrder(orderRequest);

        // Then
        assertNotNull(response);
        assertEquals("PAYMENT_FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Payment was declined"));
        assertTrue(response.getMessage().contains("Insufficient funds"));
        assertNotNull(response.getOrderId());

        // Verify RabbitMQ message was NOT sent
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void processOrder_PaymentServiceUnavailable() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Hawaiian", 3, "Test Street 3", "Bob Smith");
        
        when(restTemplate.postForObject(anyString(), any(PaymentRequest.class), eq(PaymentResponse.class)))
            .thenThrow(new RestClientException("Connection refused"));

        // When
        OrderResponse response = orderService.processOrder(orderRequest);

        // Then
        assertNotNull(response);
        assertEquals("ERROR", response.getStatus());
        assertTrue(response.getMessage().contains("Payment system is currently unavailable"));
        assertNotNull(response.getOrderId());

        // Verify RabbitMQ message was NOT sent
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void processOrder_RabbitMQFailure_OrderStillAccepted() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Quattro Formaggi", 1, "Test Street 4", "Alice Wonder");
        PaymentResponse paymentResponse = new PaymentResponse("txn789", true, "Payment successful");
        
        when(restTemplate.postForObject(anyString(), any(PaymentRequest.class), eq(PaymentResponse.class)))
            .thenReturn(paymentResponse);
        doThrow(new RuntimeException("RabbitMQ connection failed"))
            .when(rabbitTemplate).convertAndSend(anyString(), any(Object.class));

        // When
        OrderResponse response = orderService.processOrder(orderRequest);

        // Then - Order should still be accepted even if RabbitMQ fails
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.getMessage().contains("Order placed successfully"));
    }

    @Test
    void processOrder_CalculatesCorrectAmount() {
        // Given - Test different quantities
        OrderRequest orderRequest1 = new OrderRequest("Test Pizza", 1, "Address", "Customer");
        OrderRequest orderRequest2 = new OrderRequest("Test Pizza", 5, "Address", "Customer");
        PaymentResponse paymentResponse = new PaymentResponse("txn", true, "OK");
        
        when(restTemplate.postForObject(anyString(), any(PaymentRequest.class), eq(PaymentResponse.class)))
            .thenReturn(paymentResponse);

        // When
        orderService.processOrder(orderRequest1);
        orderService.processOrder(orderRequest2);

        // Then
        ArgumentCaptor<PaymentRequest> paymentCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(restTemplate, times(2)).postForObject(anyString(), paymentCaptor.capture(), eq(PaymentResponse.class));
        
        assertEquals(1 * PIZZA_PRICE, paymentCaptor.getAllValues().get(0).getAmount());
        assertEquals(5 * PIZZA_PRICE, paymentCaptor.getAllValues().get(1).getAmount());
    }
}
