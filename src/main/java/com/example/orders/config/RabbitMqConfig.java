
package com.example.orders.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // Procesamiento de pedidos
    public static final String QUEUE_PROCESS_ORDER =
            "orders.process.pdf.queue";
    public static final String EXCHANGE_ORDERS =
            "orders.direct.exchange";
    public static final String ROUTING_KEY_ORDERS =
            "orders.process.key";

    // Actualizaciones de stock publicadas por catalog
    public static final String QUEUE_STOCK_UPDATED =
            "catalog.stock.updated.queue";
    public static final String EXCHANGE_CATALOG =
            "catalog.direct.exchange";
    public static final String ROUTING_KEY_STOCK_UPDATED =
            "catalog.stock.updated.key";

    // Comandos de stock enviados a catalog
    public static final String QUEUE_STOCK_CMD =
            "catalog.stock.cmd.queue";
    public static final String ROUTING_KEY_STOCK_CMD =
            "catalog.stock.cmd.key";

    // Eventos de auditoría enviados a report
    public static final String AUDIT_TOPIC_EXCHANGE =
            "pedidos360.events.topic";

    // Cola de tareas de pedidos
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(QUEUE_PROCESS_ORDER).build();
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(EXCHANGE_ORDERS);
    }

    @Bean
    public Binding orderBinding(
            @Qualifier("orderQueue") Queue queue,
            @Qualifier("orderExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTING_KEY_ORDERS);
    }

    // Conversión de objetos Java a mensajes JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Cola para notificaciones de stock
    @Bean
    public Queue stockUpdatedQueue() {
        return QueueBuilder.durable(QUEUE_STOCK_UPDATED).build();
    }

    @Bean
    public DirectExchange catalogExchange() {
        return new DirectExchange(EXCHANGE_CATALOG);
    }

    @Bean
    public Binding stockUpdatedBinding(
            @Qualifier("stockUpdatedQueue") Queue queue,
            @Qualifier("catalogExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTING_KEY_STOCK_UPDATED);
    }

    // Cola que recibe los comandos enviados al catálogo
    @Bean
    public Queue stockCmdQueue() {
        return QueueBuilder.durable(QUEUE_STOCK_CMD).build();
    }

    @Bean
    public Binding stockCmdBinding(
            @Qualifier("stockCmdQueue") Queue queue,
            @Qualifier("catalogExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTING_KEY_STOCK_CMD);
    }

    // Exchange de auditoría compartido con report
    @Bean
    public TopicExchange auditTopicExchange() {
        return new TopicExchange(
                AUDIT_TOPIC_EXCHANGE,
                true,
                false
        );
    }
}
