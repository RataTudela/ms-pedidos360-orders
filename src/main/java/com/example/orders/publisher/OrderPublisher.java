package com.example.orders.publisher;

import com.example.orders.config.RabbitMqConfig;
import com.example.orders.model.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(Order order) {
        rabbitTemplate.convertAndSend(
            RabbitMqConfig.EXCHANGE_ORDERS,
            RabbitMqConfig.ROUTING_KEY_ORDERS,
            order
        );
    }
}