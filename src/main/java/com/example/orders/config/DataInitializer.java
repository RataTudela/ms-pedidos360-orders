package com.example.orders.config;

import com.example.orders.model.Product;
import com.example.orders.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                    new Product("PROD-001", "Notebook Corp X", 850000.00, 20),
                    new Product("PROD-002", "Mouse Inalámbrico", 15000.00, 100),
                    new Product("PROD-003", "Teclado Mecánico RGB", 45000.00, 45),
                    new Product("PROD-004", "Monitor 27 IPS 144Hz", 180000.00, 15),
                    new Product("PROD-005", "Audífonos Bluetooth Pro", 35000.00, 60),
                    new Product("PROD-006", "Silla Ergonómica Oficina", 120000.00, 10)
                ));
            }
        };
    }
}