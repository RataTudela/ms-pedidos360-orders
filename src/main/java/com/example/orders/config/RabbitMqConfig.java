package com.example.orders.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String QUEUE_PROCESS_ORDER = "orders.process.pdf.queue";
    public static final String EXCHANGE_ORDERS = "orders.direct.exchange";
    public static final String ROUTING_KEY_ORDERS = "orders.process.key";

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(QUEUE_PROCESS_ORDER).build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_ORDERS);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_ORDERS);
    }
}