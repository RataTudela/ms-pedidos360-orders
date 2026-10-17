package com.example.orders.dto;

public class StockUpdateCommand {
    private String productId;
    private Integer quantityToDeduct;

    public StockUpdateCommand() {}

    public StockUpdateCommand(String productId, Integer quantityToDeduct) {
        this.productId = productId;
        this.quantityToDeduct = quantityToDeduct;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Integer getQuantityToDeduct() { return quantityToDeduct; }
    public void setQuantityToDeduct(Integer quantityToDeduct) { this.quantityToDeduct = quantityToDeduct; }
}