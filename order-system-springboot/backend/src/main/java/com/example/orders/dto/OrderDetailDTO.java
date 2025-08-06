// 用于查询返回的订单详情（getOrderByOrderId）
package com.example.orders.dto;

import java.util.List;

public class OrderDetailDTO {
    private String orderId;
    private String userId;
    private List<OrderItemDetailDTO> items;

    // Getter and Setter methods

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItemDetailDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDetailDTO> items) {
        this.items = items;
    }
}

