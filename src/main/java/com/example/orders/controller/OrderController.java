package com.example.orders.controller;

import com.example.orders.model.Order;
import com.example.orders.repository.OrderRepository;
import com.example.orders.service.MessagingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final MessagingService messagingService;

    public OrderController(OrderRepository orderRepository, MessagingService messagingService) {
        this.orderRepository = orderRepository;
        this.messagingService = messagingService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_OT.Create') and hasAnyRole('Admin', 'Operator', 'Customer')")
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        if (order.getStatus() == null) {
            order.setStatus("CREADO");
        }
        orderRepository.save(order);
        messagingService.publishKafkaEvent(order);
        messagingService.sendRabbitTask(order.getId());

        return ResponseEntity.ok("Order " + order.getId() + " created successfully.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("(hasAuthority('SCOPE_OT.Create') or hasAuthority('SCOPE_OT.Update')) and hasAnyRole('Admin', 'Operator')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id, @RequestParam String newStatus) {
        return orderRepository.findById(id).map(order -> {
            String currentStatus = order.getStatus();

            // Regla de Negocio: No despachar sin haber sido aceptado previamente
            if ("DESPACHADO".equalsIgnoreCase(newStatus) && !"ACEPTADO".equalsIgnoreCase(currentStatus) && !"EN_PREPARACION".equalsIgnoreCase(currentStatus)) {
                return ResponseEntity.badRequest().body("Error: No se puede despachar un pedido que no ha sido ACEPTADO.");
            }

            order.setStatus(newStatus.toUpperCase());
            orderRepository.save(order);

            messagingService.publishKafkaEvent(order);
            messagingService.sendRabbitTask(order.getId());

            return ResponseEntity.ok(order);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}