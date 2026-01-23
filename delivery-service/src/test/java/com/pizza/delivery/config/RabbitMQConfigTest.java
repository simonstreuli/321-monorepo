package com.pizza.delivery.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RabbitMQConfig Tests")
class RabbitMQConfigTest {

    private final RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();

    @Test
    @DisplayName("Should create durable order ready queue")
    void shouldCreateDurableOrderReadyQueue() {
        // When
        Queue queue = rabbitMQConfig.orderReadyQueue();

        // Then
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.ORDER_READY_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    @DisplayName("ORDER_READY_QUEUE constant should be 'order.ready'")
    void orderReadyQueueConstantShouldBeCorrect() {
        // Then
        assertEquals("order.ready", RabbitMQConfig.ORDER_READY_QUEUE);
    }

    @Test
    @DisplayName("Should create JSON message converter")
    void shouldCreateJsonMessageConverter() {
        // When
        MessageConverter converter = rabbitMQConfig.jsonMessageConverter();

        // Then
        assertNotNull(converter);
        assertInstanceOf(Jackson2JsonMessageConverter.class, converter);
    }
}
