package com.pizza.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class PaymentServiceApplicationTest {

    @Test
    void main_shouldRunSpringApplication() {
        try (var mockedSpringApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
            String[] args = new String[] { "arg1", "arg2" };
            PaymentServiceApplication.main(args);
            mockedSpringApplication.verify(() -> SpringApplication.run(PaymentServiceApplication.class, args), times(1));
        }
    }
}