
package com.example.orders.service;

import com.example.orders.config.KafkaConfig;
import com.example.orders.config.RabbitMqConfig;
import com.example.orders.dto.StockUpdateCommand;
import com.example.orders.model.Order;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessagingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RabbitTemplate rabbitTemplate;

    public MessagingService(
            KafkaTemplate<String, Object> kafkaTemplate,
            RabbitTemplate rabbitTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    // Publicar eventos del pedido en Kafka
    public void publishKafkaEvent(Order order) {
        kafkaTemplate.send(
                KafkaConfig.TOPIC_ORDER_EVENTS,
                order.getId(),
                order
        );
    }

    // Enviar la tarea de procesamiento del pedido a RabbitMQ
    public void sendRabbitTask(String orderId) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_ORDERS,
                RabbitMqConfig.ROUTING_KEY_ORDERS,
                orderId
        );
    }

    // NUEVO MÉTODO PARA ENVIAR EL COMANDO A CATÁLOGO
    public void publishStockUpdateCommand(String productId, Integer quantityToDeduct) {
        StockUpdateCommand command = new StockUpdateCommand(productId, quantityToDeduct);
        rabbitTemplate.convertAndSend(
            RabbitMqConfig.EXCHANGE_CATALOG,
            RabbitMqConfig.ROUTING_KEY_STOCK_CMD,
            command
        );
    }

    // Escuchar eventos de pedidos en Kafka
    @KafkaListener(
            topics = KafkaConfig.TOPIC_ORDER_EVENTS,
            groupId = "orders-group"
    )
    public void listenKafkaEvent(Order order) {
        System.out.println(
                "Kafka event received for Order ID: " + order.getId()
        );
    }

    // Escuchar tareas de pedidos en RabbitMQ
    @RabbitListener(
            queues = RabbitMqConfig.QUEUE_PROCESS_ORDER
    )
    public void processRabbitTask(String orderId) {
        System.out.println(
                "Processing RabbitMQ queue for Order ID: " + orderId
        );
    }
}
