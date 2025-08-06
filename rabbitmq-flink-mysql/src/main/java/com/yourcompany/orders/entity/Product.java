package com.yourcompany.orders.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.yourcompany.orders.common.ProductStatus;

@Data
public class Product {
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal unitPrice;
    private Integer stock;
    private String imageUrl; // 对应 image_url
    private Long categoryId; // 对应 category_id
    private ProductStatus status; // 或者使用枚举 String status;
    private LocalDateTime createdAt; // 对应 created_at
    private LocalDateTime updatedAt; // 对应 updated_at

    // Constructors
    public Product() {
    }

    public Product(Long id, String name, String description, BigDecimal price, Integer stock, String imageUrl, Long categoryId, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.productId = id;
        this.productName = name;
        this.description = description;
        this.unitPrice = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.status = ProductStatus.fromString(status);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters (省略，请根据需要生成)
    // 例如：
    // public Long getId() { return id; }
    // public void setId(Long id) { this.id = id; }
    // ... 其他字段的 Getter 和 Setter

    @Override
    public String toString() {
        return "Product{" +
               "id=" + productId +
               ", name='" + productName + '\'' +
               ", description='" + description + '\'' +
               ", price=" + unitPrice +
               ", stock=" + stock +
               ", imageUrl='" + imageUrl + '\'' +
               ", categoryId=" + categoryId +
               ", status='" + status + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
