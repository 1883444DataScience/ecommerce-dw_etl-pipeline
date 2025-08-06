package com.example.orders.entity;

import lombok.Data;
import java.math.BigDecimal;

public class OrderItem {
    private Long id;
    private Long orderId; // 对应 order_id
    private Long productId; // 对应 product_id
    private String productName; // 对应 product_name
    private BigDecimal unitPrice; // 对应 unit_price
    private Integer quantity;
    private BigDecimal totalPrice; // 对应 total_price

    // 构造函数
    public OrderItem() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    @Override
    public String toString() {
        return "OrderItem{" +
               "id=" + id +
               ", orderId=" + orderId +
               ", productId=" + productId +
               ", productName='" + productName + '\'' +
               ", quantity=" + quantity +
               '}';
    }
}
