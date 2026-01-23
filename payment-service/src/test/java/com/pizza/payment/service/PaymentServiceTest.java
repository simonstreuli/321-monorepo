package com.pizza.payment.service;

import com.pizza.models.PaymentRequest;
import com.pizza.models.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        ReflectionTestUtils.setField(paymentService, "failureRate", 0.0d);
        ReflectionTestUtils.setField(paymentService, "delayMin", 0);
        ReflectionTestUtils.setField(paymentService, "delayMax", 1);
    }

    @Test
    void processPayment_shouldReturnSuccess_whenRandomDoesNotFail() {
        PaymentRequest request = mock(PaymentRequest.class);
        when(request.getOrderId()).thenReturn("order-1");
        when(request.getAmount()).thenReturn(100.0);

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getTransactionId());
        assertEquals("Payment processed successfully", response.getMessage());
    }

    @Test
    void processPayment_shouldReturnFailure_whenRandomFails() {
        paymentService = new PaymentService();
        ReflectionTestUtils.setField(paymentService, "failureRate", 1.0d);
        ReflectionTestUtils.setField(paymentService, "delayMin", 0);
        ReflectionTestUtils.setField(paymentService, "delayMax", 1);

        PaymentRequest request = mock(PaymentRequest.class);
        when(request.getOrderId()).thenReturn("order-2");
        when(request.getAmount()).thenReturn(50.0);

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNull(response.getTransactionId());
        assertEquals("Payment declined by bank. Please try a different payment method.", response.getMessage());
    }
}