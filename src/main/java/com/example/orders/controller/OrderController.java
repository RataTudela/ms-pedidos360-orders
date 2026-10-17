package com.example.orders.controller;

import com.example.orders.model.Order;
import com.example.orders.model.OrderItem;
import com.example.orders.repository.OrderRepository;
import com.example.orders.service.MessagingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final MessagingService messagingService;
    private final RestTemplate restTemplate = new RestTemplate();

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

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                try {
                    String catalogUrl = "http://ms-catalog:8087/api/catalog/productos/" + item.getProductId();
                    
                    // 1. Obtener el producto actual en un Map para leer su stock real
                    @SuppressWarnings("unchecked")
                    Map<String, Object> prod = restTemplate.getForObject(catalogUrl, Map.class);
                    
                    if (prod != null && prod.containsKey("stock")) {
                        int stockActual = ((Number) prod.get("stock")).intValue();
                        
                        // 2. Calcular la resta (Stock Actual - Cantidad Comprada)
                        int nuevoStock = Math.max(0, stockActual - item.getQuantity());
                        
                        // 3. Enviar actualización usando PUT (evita el error de PATCH)
                        restTemplate.put(catalogUrl + "/stock?stock=" + nuevoStock, null);
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo actualizar el stock en Catálogo: " + e.getMessage());
                }
            }
        }

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