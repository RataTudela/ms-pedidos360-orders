package com.example.orders.publisher;

import com.example.orders.config.RabbitMqConfig;
import com.example.orders.dto.StockUpdateCommand;
import com.example.orders.model.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

        public void publishStockUpdateCommand(String productId, Integer quantityToDeduct) {
        StockUpdateCommand command = new StockUpdateCommand(productId, quantityToDeduct);
        rabbitTemplate.convertAndSend(
            "catalog.direct.exchange", 
            "catalog.stock.cmd.key",   
            command
        );
    }
}