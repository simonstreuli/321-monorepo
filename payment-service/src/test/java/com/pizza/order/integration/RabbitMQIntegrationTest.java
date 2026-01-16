package com.pizza.order.integration;

import com.pizza.order.config.RabbitMQConfig;
import com.pizza.order.model.OrderPlacedEvent;
import com.pizza.order.model.OrderRequest;
import com.pizza.order.model.PaymentResponse;
import com.pizza.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class RabbitMQIntegrationTest {

    @Autowired
    private OrderService orderService;

    @MockBean
    private RestTemplate restTemplate;

    @SpyBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void processOrder_Success_SendsMessageToRabbitMQ() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Test Street 1", "John Doe");
        PaymentResponse paymentResponse = new PaymentResponse("txn123", true, "Payment successful");
        
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(paymentResponse);

        // When
        orderService.processOrder(orderRequest);

        // Then
        ArgumentCaptor<OrderPlacedEvent> eventCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitMQConfig.ORDER_PLACED_QUEUE),
            eventCaptor.capture()
        );

        OrderPlacedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertNotNull(capturedEvent.getOrderId());
        assertEquals("Margherita", capturedEvent.getPizza());
        assertEquals(2, capturedEvent.getQuantity());
        assertEquals("Test Street 1", capturedEvent.getAddress());
        assertEquals("John Doe", capturedEvent.getCustomerName());
        assertNotNull(capturedEvent.getTimestamp());
    }

    @Test
    void processOrder_PaymentFailed_DoesNotSendMessageToRabbitMQ() {
        // Given
        OrderRequest orderRequest = new OrderRequest("Pepperoni", 1, "Test Street 2", "Jane Doe");
        PaymentResponse paymentResponse = new PaymentResponse("txn456", false, "Insufficient funds");
        
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(paymentResponse);

        // When
        orderService.processOrder(orderRequest);

        // Then
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void processOrder_MultipleOrders_SendsMultipleMessages() {
        // Given
        PaymentResponse paymentResponse = new PaymentResponse("txn", true, "OK");
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(paymentResponse);

        OrderRequest order1 = new OrderRequest("Margherita", 1, "Address 1", "Customer 1");
        OrderRequest order2 = new OrderRequest("Pepperoni", 2, "Address 2", "Customer 2");
        OrderRequest order3 = new OrderRequest("Hawaiian", 3, "Address 3", "Customer 3");

        // When
        orderService.processOrder(order1);
        orderService.processOrder(order2);
        orderService.processOrder(order3);

        // Then
        verify(rabbitTemplate, times(3)).convertAndSend(eq(RabbitMQConfig.ORDER_PLACED_QUEUE), any(OrderPlacedEvent.class));
    }

    @Test
    void rabbitMQConfig_QueueExists() {
        // Given
        RabbitMQConfig config = new RabbitMQConfig();

        // When
        var queue = config.orderPlacedQueue();

        // Then
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.ORDER_PLACED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }
}
