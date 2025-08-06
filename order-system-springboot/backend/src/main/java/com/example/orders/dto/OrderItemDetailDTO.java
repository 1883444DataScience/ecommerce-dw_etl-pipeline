package com.example.orders.dto;

// 用于订单详情返回
public class OrderItemDetailDTO {
    private String productId;
    private Integer quantity;
    private String unitPrice; // DTO 中价格仍然是 String
    // totalAmount 通常由后端计算，也可以从前端接收用于校验

    // 构造函数
    public OrderItemDetailDTO() {}

    public OrderItemDetailDTO(String productId, Integer quantity, String unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getUnitPrice() { return unitPrice; }
    public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }

    @Override
    public String toString() {
        return "OrderItemDetailDTO{" +
               "productId='" + productId + '\'' +
               ", quantity=" + quantity +
               ", unitPrice='" + unitPrice + '\'' +
               '}';
    }
}
