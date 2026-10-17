package com.example.orders.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String QUEUE_PROCESS_ORDER = "orders.process.pdf.queue";
    public static final String EXCHANGE_ORDERS = "orders.direct.exchange";
    public static final String ROUTING_KEY_ORDERS = "orders.process.key";

    public static final String QUEUE_STOCK_UPDATED = "catalog.stock.updated.queue";
    public static final String EXCHANGE_CATALOG = "catalog.direct.exchange";
    public static final String ROUTING_KEY_STOCK_UPDATED = "catalog.stock.updated.key";


    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(QUEUE_PROCESS_ORDER).build();
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(EXCHANGE_ORDERS);
    }

    @Bean
    public Binding orderBinding(@Qualifier("orderQueue") Queue queue, 
                                @Qualifier("orderExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_ORDERS);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue stockUpdatedQueue() {
        return QueueBuilder.durable(QUEUE_STOCK_UPDATED).build();
    }

    @Bean
    public DirectExchange catalogExchange() {
        return new DirectExchange(EXCHANGE_CATALOG);
    }

    @Bean
    public Binding stockUpdatedBinding(@Qualifier("stockUpdatedQueue") Queue queue, 
                                        @Qualifier("catalogExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_STOCK_UPDATED);
    }
}