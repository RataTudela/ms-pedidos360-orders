package com.example.orders.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StockEventListener {
    @RabbitListener(queues = "catalog.stock.updated.queue")
    public void handleStockUpdate(Object stockEvent) {
        System.out.println("Notificación recibida de Catálogo: " + stockEvent);
    }
}