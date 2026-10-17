package com.example.orders.publisher;

import com.example.orders.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public AuditEventPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCreated(Order order) {
        Map<String, Object> event = new LinkedHashMap<>();

        event.put("eventId", UUID.randomUUID().toString());
        event.put("type", "ORDER_CREATED");
        event.put("orderId", String.valueOf(order.getId()));
        event.put("actor", order.getCustomer());
        event.put("timestamp", LocalDateTime.now().toString());

        try {
            String json = objectMapper.writeValueAsString(event);

            MessageProperties properties = new MessageProperties();
            properties.setContentType(
                    MessageProperties.CONTENT_TYPE_JSON
            );

            Message message = new Message(
                    json.getBytes(StandardCharsets.UTF_8),
                    properties
            );

            rabbitTemplate.send(
                    "pedidos360.events.topic",
                    "orders.audit.created",
                    message
            );

            System.out.println(
                    "[RabbitMQ] Evento de auditoría publicado: " + json
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo publicar el evento de auditoría",
                    e
            );
        }
    }
}