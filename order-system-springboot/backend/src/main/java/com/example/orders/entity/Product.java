package com.example.orders.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.orders.common.ProductStatus;

@Data
public class Product {
    private Long id; // product_id
    private String name;
    private String description;
    private BigDecimal price;
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
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
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
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", price=" + price +
               ", stock=" + stock +
               ", imageUrl='" + imageUrl + '\'' +
               ", categoryId=" + categoryId +
               ", status='" + status + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
