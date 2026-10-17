package com.example.orders.listener;

import com.example.orders.dto.StockUpdatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StockEventListener {

    @RabbitListener(queues = "catalog.stock.updated.queue")
    public void handleStockUpdate(StockUpdatedEvent stockEvent) {
        System.out.println("==================================================");
        System.out.println(" [RabbitMQ] Notificación recibida de Catálogo:");
        System.out.println("   - Producto ID : " + stockEvent.getProductId());
        System.out.println("   - Nuevo Stock : " + stockEvent.getNewStock());
        System.out.println("==================================================");
    }
}