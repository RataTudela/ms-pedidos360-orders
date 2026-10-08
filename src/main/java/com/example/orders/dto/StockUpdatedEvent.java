package com.example.orders.dto;

public class StockUpdatedEvent {
    private String productId;   
    private Integer newStock;

    public StockUpdatedEvent() {}

    public StockUpdatedEvent(String productId, Integer newStock) {
        this.productId = productId;
        this.newStock = newStock;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getNewStock() { return newStock; }
    public void setNewStock(Integer newStock) { this.newStock = newStock; }
}