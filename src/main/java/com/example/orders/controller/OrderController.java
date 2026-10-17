
package com.example.orders.controller;

import com.example.orders.model.Order;
import com.example.orders.model.OrderItem;
import com.example.orders.repository.OrderRepository;
import com.example.orders.service.MessagingService;
import com.example.orders.publisher.AuditEventPublisher;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final MessagingService messagingService;
    private final AuditEventPublisher auditEventPublisher;

    public OrderController(
            OrderRepository orderRepository,
            MessagingService messagingService,
            AuditEventPublisher auditEventPublisher) {
        this.orderRepository = orderRepository;
        this.messagingService = messagingService;
        this.auditEventPublisher = auditEventPublisher;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('SCOPE_OT.Create') and " +
            "hasAnyRole('Admin', 'Operator', 'Customer')"
    )
    public ResponseEntity<String> createOrder(
            @RequestBody Order order) {

        if (order.getStatus() == null) {
            order.setStatus("CREADO");
        }

        // 1. Guardar el pedido en la base de datos.
        Order saved = orderRepository.save(order);

        // 2. Publicar el evento de auditoría para report.
        auditEventPublisher.publishOrderCreated(saved);

        // 3. Enviar los comandos de stock a catalog.
        if (saved.getItems() != null) {
            for (OrderItem item : saved.getItems()) {
                messagingService.publishStockUpdateCommand(
                        item.getProductId(),
                        item.getQuantity()
                );
            }
        }

        // 4. Mantener la publicación del evento en Kafka.
        messagingService.publishKafkaEvent(saved);

        // 5. Mantener la tarea de procesamiento en RabbitMQ.
        messagingService.sendRabbitTask(saved.getId());

        return ResponseEntity.ok(
                "Order " + saved.getId() + " created successfully."
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable String id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize(
            "(hasAuthority('SCOPE_OT.Create') or " +
            "hasAuthority('SCOPE_OT.Update')) and " +
            "hasAnyRole('Admin', 'Operator')"
    )
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String id,
            @RequestParam String newStatus) {

        return orderRepository.findById(id).map(order -> {
            String currentStatus = order.getStatus();

            if ("DESPACHADO".equalsIgnoreCase(newStatus)
                    && !"ACEPTADO".equalsIgnoreCase(currentStatus)
                    && !"EN_PREPARACION".equalsIgnoreCase(currentStatus)) {
                return ResponseEntity.badRequest().body(
                        "Error: No se puede despachar un pedido " +
                        "que no ha sido ACEPTADO."
                );
            }

            order.setStatus(newStatus.toUpperCase());
            Order saved = orderRepository.save(order);

            messagingService.publishKafkaEvent(saved);
            messagingService.sendRabbitTask(saved.getId());

            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
