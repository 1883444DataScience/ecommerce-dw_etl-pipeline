package com.example.orders.common; // 请确保这个包名和你在 Order.java 中 import 的包名一致

/**
 * 订单状态枚举。
 * 包含订单的常见生命周期状态。
 */
public enum OrderStatus {
    /**
     * 订单已创建，等待处理。
     */
    PENDING("待处理"),

    /**
     * 订单正在处理中（例如，库存扣减、支付确认中）。
     */
    PROCESSING("处理中"),

    /**
     * 订单已发货。
     */
    SHIPPED("已发货"),

    /**
     * 订单已送达。
     */
    DELIVERED("已送达"),

    /**
     * 订单已取消。
     */
    CANCELLED("已取消"),

    /**
     * 订单已退款。
     */
    REFUNDED("已退款"),
    
    /**
     * 订单未知态。
     */
    UNKNOWN("未知中"),

    /**
     * 订单新产生。
     */
    NEW("新订单");

    private final String description; // 用于存储状态的中文描述

    // 构造函数
    OrderStatus(String description) {
        this.description = description;
    }

    // 获取状态描述
    public String getDescription() {
        return description;
    }

    /**
     * 根据字符串值获取对应的 OrderStatus 枚举实例。
     * 该方法不区分大小写地匹配枚举常量的名称（例如 "pending" 会匹配 PENDING）。
     *
     * @param text 要转换的字符串，例如 "PENDING", "shipped"
     * @return 对应的 OrderStatus 枚举实例
     * @throws IllegalArgumentException 如果传入的字符串无法匹配任何有效的订单状态
     */
    public static OrderStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("订单状态字符串不能为空。");
        }
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("无法识别的订单状态: " + text + "。有效状态为: " +
                java.util.Arrays.toString(OrderStatus.values()));
    }
}
