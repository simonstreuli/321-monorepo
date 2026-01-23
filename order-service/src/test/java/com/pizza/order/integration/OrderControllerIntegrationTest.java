package com.pizza.order.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizza.models.OrderRequest;
import com.pizza.models.OrderResponse;
import com.pizza.models.PaymentResponse;
import com.pizza.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void createOrder_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Test Street 1", "John Doe");
        OrderResponse orderResponse = new OrderResponse("order123", "SUCCESS", "Order placed successfully");
        
        when(orderService.processOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value("order123"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Order placed successfully"));
    }

    @Test
    void createOrder_PaymentFailed_ReturnsPaymentRequired() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Pepperoni", 1, "Test Street 2", "Jane Doe");
        OrderResponse orderResponse = new OrderResponse("order456", "PAYMENT_FAILED", "Payment declined");
        
        when(orderService.processOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.status").value("PAYMENT_FAILED"));
    }

    @Test
    void createOrder_ServiceError_ReturnsServiceUnavailable() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Hawaiian", 3, "Test Street 3", "Bob Smith");
        OrderResponse orderResponse = new OrderResponse("order789", "ERROR", "Service unavailable");
        
        when(orderService.processOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void createOrder_InvalidRequest_MissingPizza_ReturnsBadRequest() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest(null, 2, "Test Street 1", "John Doe");

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pizza").value("Pizza type is required"));
    }

    @Test
    void createOrder_InvalidRequest_NullQuantity_ReturnsBadRequest() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", null, "Test Street 1", "John Doe");

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity").value("Quantity is required"));
    }

    @Test
    void createOrder_InvalidRequest_NegativeQuantity_ReturnsBadRequest() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", -1, "Test Street 1", "John Doe");

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity").value("Quantity must be positive"));
    }

    @Test
    void createOrder_InvalidRequest_MissingAddress_ReturnsBadRequest() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, null, "John Doe");

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.address").value("Delivery address is required"));
    }

    @Test
    void createOrder_InvalidRequest_MissingCustomerName_ReturnsBadRequest() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Test Street 1", null);

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.customerName").value("Customer name is required"));
    }

    @Test
    void createOrder_InvalidRequest_EmptyBody_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void health_ReturnsOk() throws Exception {
        // When & Then
        mockMvc.perform(get("/orders/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order Service is running"));
    }
}
