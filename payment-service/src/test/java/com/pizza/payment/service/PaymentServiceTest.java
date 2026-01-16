package com.pizza.payment.service;

import com.pizza.payment.model.PaymentRequest;
import com.pizza.payment.model.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Random;
import java.util.UUID;

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

    @Test
    void processPayment_shouldHandleInterruptedException() throws Exception {
        PaymentService service = new PaymentService();
        ReflectionTestUtils.setField(service, "failureRate", 0.0d);
        ReflectionTestUtils.setField(service, "delayMin", 0);
        ReflectionTestUtils.setField(service, "delayMax", 1);

        PaymentRequest request = mock(PaymentRequest.class);
        when(request.getOrderId()).thenReturn("order-3");
        when(request.getAmount()).thenReturn(75.0);

        try (MockedStatic<Thread> threadMock = Mockito.mockStatic(Thread.class)) {
            threadMock.when(() -> Thread.sleep(anyLong())).thenThrow(new InterruptedException());
            PaymentResponse response = service.processPayment(request);
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertNotNull(response.getTransactionId());
            assertEquals("Payment processed successfully", response.getMessage());
        }
    }
}