package com.pizza.payment.controller;

import com.pizza.payment.model.PaymentRequest;
import com.pizza.payment.model.PaymentResponse;
import com.pizza.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentRequest validPaymentRequest;
    private PaymentResponse successPaymentResponse;
    private PaymentResponse failurePaymentResponse;

    @BeforeEach
    void setUp() {
        validPaymentRequest = new PaymentRequest("order-1", "John Doe", 100.0);
        successPaymentResponse = new PaymentResponse("txn-123", true, "Payment processed successfully");
        failurePaymentResponse = new PaymentResponse(null, false,
                "Payment declined by bank. Please try a different payment method.");
    }

    @Test
    void testProcessPayment_Success() {
        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(successPaymentResponse);

        ResponseEntity<PaymentResponse> response = paymentController.processPayment(validPaymentRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("txn-123", response.getBody().getTransactionId());
        assertEquals("Payment processed successfully", response.getBody().getMessage());
    }

    @Test
    void testProcessPayment_Failure() {
        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(failurePaymentResponse);

        PaymentRequest failureRequest = new PaymentRequest("order-2", "Jane Smith", 50.0);
        ResponseEntity<PaymentResponse> response = paymentController.processPayment(failureRequest);

        assertEquals(HttpStatus.PAYMENT_REQUIRED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getTransactionId());
        assertEquals("Payment declined by bank. Please try a different payment method.",
                response.getBody().getMessage());
    }

    @Test
    void testHealth() {
        ResponseEntity<String> response = paymentController.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Payment Service is running", response.getBody());
    }

    @Test
    void testProcessPayment_WithDifferentAmounts() {
        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(successPaymentResponse);

        PaymentRequest request1 = new PaymentRequest("order-100", "Alice Johnson", 25.50);
        ResponseEntity<PaymentResponse> response1 = paymentController.processPayment(request1);

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertTrue(response1.getBody().isSuccess());

        PaymentRequest request2 = new PaymentRequest("order-200", "Bob Wilson", 199.99);
        ResponseEntity<PaymentResponse> response2 = paymentController.processPayment(request2);

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertTrue(response2.getBody().isSuccess());
    }
}
