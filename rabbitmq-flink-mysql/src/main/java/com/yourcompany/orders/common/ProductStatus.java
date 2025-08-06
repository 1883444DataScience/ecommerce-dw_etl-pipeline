// 例如：com/example/orders/common/ProductStatus.java
package com.yourcompany.orders.common; // 或者 com.example.orders.entity;

public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    DRAFT,
    ARCHIVED;

    // 如果你需要从字符串转换，可以添加一个静态方法
    public static ProductStatus fromString(String text) {
        for (ProductStatus b : ProductStatus.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
