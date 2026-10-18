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

    // Constante para MessagingService.java
    public static final String ROUTING_KEY_STOCK_CMD = "catalog.stock.cmd.key";

    // Constantes para la Dead Letter Queue (DLQ) de Orders
    public static final String ORDERS_DLX_EXCHANGE = "orders.dlx.exchange";
    public static final String ORDERS_STOCK_UPDATED_DLQ = "orders.stock.updated.dlq";
    public static final String ORDERS_DLK_KEY = "orders.dlk.key";

    public static final String EXCHANGE_EVENTS_TOPIC = "pedidos360.events.topic";

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
        return QueueBuilder.durable(QUEUE_STOCK_UPDATED)
                .withArgument("x-dead-letter-exchange", ORDERS_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDERS_DLK_KEY)
                .build();
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

    @Bean
    public Queue stockCmdQueue() {
        return QueueBuilder.durable("catalog.stock.cmd.queue").build();
    }

    @Bean
    public Binding stockCmdBinding(@Qualifier("stockCmdQueue") Queue queue, 
                                   @Qualifier("catalogExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_STOCK_CMD);
    }

    // Definición del DLX y DLQ para Orders
    @Bean
    public DirectExchange ordersDeadLetterExchange() {
        return new DirectExchange(ORDERS_DLX_EXCHANGE);
    }

    @Bean
    public Queue ordersStockUpdatedDlq() {
        return QueueBuilder.durable(ORDERS_STOCK_UPDATED_DLQ).build();
    }

    @Bean
    public Binding ordersDeadLetterBinding(@Qualifier("ordersStockUpdatedDlq") Queue queue,
                                            @Qualifier("ordersDeadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ORDERS_DLK_KEY);
    }

    @Bean
    public TopicExchange eventsTopicExchange() {
        return new TopicExchange(EXCHANGE_EVENTS_TOPIC);
    }
}