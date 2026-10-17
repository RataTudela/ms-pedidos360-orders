package com.example.orders.repository;

import com.example.orders.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Override
    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"items"})
    List<Order> findAll();

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") String id);
}