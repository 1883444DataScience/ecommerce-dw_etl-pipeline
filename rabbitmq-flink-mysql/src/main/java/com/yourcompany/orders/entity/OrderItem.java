package com.yourcompany.orders.entity;

import lombok.Data;
import java.math.BigDecimal;

public class OrderItem {
    private Long id; // 该订单下该商品item的唯一id, AUTO INCREMENT
    private Long orderId; // 商品所在订单的order_id
    private Long productId; // 对应 product_id, 单个商品只有一个product_id 但库存情况下可以多次消费 从而有了不同的id
    private String productName; // 对应 product_name
    private BigDecimal unitPrice; // 对应 unit_price
    private Integer quantity;
    private BigDecimal totalAmount; // 对应 total_price

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
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

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
