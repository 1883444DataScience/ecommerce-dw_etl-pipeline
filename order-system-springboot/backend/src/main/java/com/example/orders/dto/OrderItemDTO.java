package com.example.orders.dto;

import java.io.Serializable; // <--- 新增：导入 Serializable 接口
import java.math.BigDecimal;

// 订单项DTO
public class OrderItemDTO implements Serializable {
    private static final long serialVersionUID = 1L; // 推荐添加 serialVersionUID

    private Long id;
    private Long productId;
    private String productName;
    private String unitPrice; // 价格字段在DTO中可为String
    private Integer quantity;
    private String totalPrice; // 价格字段在DTO中可为String

    // 构造函数
    public OrderItemDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getUnitPrice() { return unitPrice; }
    public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getTotalPrice() { return totalPrice; }
    public void setTotalPrice(String totalPrice) { this.totalPrice = totalPrice; }

    @Override
    public String toString() {
        return "OrderItemDTO{" +
               "id=" + id +
               ", productId=" + productId +
               ", productName='" + productName + '\'' +
               ", quantity=" + quantity +
               '}';
    }
}
