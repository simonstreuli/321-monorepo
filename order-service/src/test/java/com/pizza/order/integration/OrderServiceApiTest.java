package com.pizza.order.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizza.order.model.OrderRequest;
import com.pizza.order.model.PaymentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderServiceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void endToEnd_CreateOrder_Success() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Margherita", 2, "Main Street 10", "John Doe");
        PaymentResponse paymentResponse = new PaymentResponse("txn123", true, "Payment successful");
        
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(paymentResponse);

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value(containsString("Order placed successfully")))
                .andExpect(jsonPath("$.message").value(containsString("order ID")));
    }

    @Test
    void endToEnd_CreateOrder_PaymentDeclined() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Pepperoni", 1, "Oak Street 5", "Jane Smith");
        PaymentResponse paymentResponse = new PaymentResponse("txn456", false, "Insufficient funds");
        
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(paymentResponse);

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PAYMENT_FAILED"))
                .andExpect(jsonPath("$.message").value(containsString("Payment was declined")))
                .andExpect(jsonPath("$.message").value(containsString("Insufficient funds")));
    }

    @Test
    void endToEnd_CreateOrder_PaymentServiceDown() throws Exception {
        // Given
        OrderRequest orderRequest = new OrderRequest("Hawaiian", 3, "Pine Street 15", "Bob Johnson");
        
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenThrow(new RestClientException("Connection timeout"));

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Payment system is currently unavailable")));
    }

    @Test
    void endToEnd_MultipleOrders_DifferentQuantities() throws Exception {
        // Given
        PaymentResponse successResponse = new PaymentResponse("txn", true, "OK");
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(successResponse);

        // Test with quantity 1
        OrderRequest order1 = new OrderRequest("Margherita", 1, "Street 1", "Customer 1");
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // Test with quantity 5
        OrderRequest order2 = new OrderRequest("Pepperoni", 5, "Street 2", "Customer 2");
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // Test with quantity 10
        OrderRequest order3 = new OrderRequest("Hawaiian", 10, "Street 3", "Customer 3");
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void endToEnd_ValidationErrors_AllFields() throws Exception {
        // Given - Invalid order with all fields invalid
        String invalidJson = """
            {
                "pizza": "",
                "quantity": -1,
                "address": "",
                "customerName": ""
            }
            """;

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pizza").exists())
                .andExpect(jsonPath("$.quantity").exists())
                .andExpect(jsonPath("$.address").exists())
                .andExpect(jsonPath("$.customerName").exists());
    }

    @Test
    void endToEnd_PizzaTypes_VariousNames() throws Exception {
        // Given
        PaymentResponse successResponse = new PaymentResponse("txn", true, "OK");
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(successResponse);

        String[] pizzaTypes = {
            "Margherita", "Pepperoni", "Hawaiian", "Quattro Formaggi",
            "Diavola", "Vegetariana", "Marinara", "Capricciosa"
        };

        // Test each pizza type
        for (String pizzaType : pizzaTypes) {
            OrderRequest order = new OrderRequest(pizzaType, 1, "Test Street", "Test Customer");
            mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(order)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SUCCESS"));
        }
    }

    @Test
    void endToEnd_LongCustomerNames_AndAddresses() throws Exception {
        // Given
        PaymentResponse successResponse = new PaymentResponse("txn", true, "OK");
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(successResponse);

        String longName = "John William Alexander Montgomery the Third";
        String longAddress = "1234 Very Long Street Name Avenue, Apartment 567, Building C, City Name, State, 12345-6789";
        
        OrderRequest order = new OrderRequest("Margherita", 2, longAddress, longName);

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void healthCheck_ReturnsOk() throws Exception {
        // When & Then
        mockMvc.perform(get("/orders/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order Service is running"));
    }

    @Test
    void endToEnd_SpecialCharacters_InFields() throws Exception {
        // Given
        PaymentResponse successResponse = new PaymentResponse("txn", true, "OK");
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(successResponse);

        OrderRequest order = new OrderRequest(
            "Spéciàl Pîzzä",
            2,
            "Straße 123, Zürich, Schweiz",
            "Müller-Späth"
        );

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void endToEnd_MaxQuantity() throws Exception {
        // Given
        PaymentResponse successResponse = new PaymentResponse("txn", true, "OK");
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponse.class)))
            .thenReturn(successResponse);

        OrderRequest order = new OrderRequest("Margherita", 100, "Test Street", "Test Customer");

        // When & Then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
