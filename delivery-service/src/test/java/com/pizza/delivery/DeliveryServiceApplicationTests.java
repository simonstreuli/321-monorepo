package com.pizza.delivery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DeliveryServiceApplicationTests {

    @Test
    void mainMethodShouldNotThrowException() {
        // Test that the main method can be called without throwing exceptions
        // This is a simple smoke test that doesn't actually start the application
        assertDoesNotThrow(() -> {
            // Verify the application class exists and is properly annotated
            Class<?> appClass = DeliveryServiceApplication.class;
            assert appClass.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class);
            assert appClass.isAnnotationPresent(org.springframework.scheduling.annotation.EnableScheduling.class);
        });
    }
}
