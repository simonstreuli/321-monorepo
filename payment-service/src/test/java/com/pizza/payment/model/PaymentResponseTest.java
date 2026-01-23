package com.pizza.payment.model;

import com.pizza.models.PaymentResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentResponseTest {

    @Test
    void testNoArgsConstructor() {
        PaymentResponse response = new PaymentResponse();
        assertNull(response.getTransactionId());
        assertFalse(response.isSuccess());
        assertNull(response.getMessage());
    }

    @Test
    void testAllArgsConstructor() {
        PaymentResponse response = new PaymentResponse("tx123", true, "Payment successful");
        assertEquals("tx123", response.getTransactionId());
        assertTrue(response.isSuccess());
        assertEquals("Payment successful", response.getMessage());
    }

    @Test
    void testSettersAndGetters() {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId("tx456");
        response.setSuccess(false);
        response.setMessage("Payment failed");

        assertEquals("tx456", response.getTransactionId());
        assertFalse(response.isSuccess());
        assertEquals("Payment failed", response.getMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        PaymentResponse response1 = new PaymentResponse("tx789", true, "Done");
        PaymentResponse response2 = new PaymentResponse("tx789", true, "Done");
        PaymentResponse response3 = new PaymentResponse("tx000", false, "Failed");

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, response3);
    }

    @Test
    void testToString() {
        PaymentResponse response = new PaymentResponse("tx111", false, "Declined");
        String str = response.toString();
        assertTrue(str.contains("tx111"));
        assertTrue(str.contains("false"));
        assertTrue(str.contains("Declined"));
    }
}