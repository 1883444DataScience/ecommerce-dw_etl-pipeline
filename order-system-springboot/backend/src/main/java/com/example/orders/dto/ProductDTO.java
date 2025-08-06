package com.example.orders.dto;

// 不需要导入 java.math.BigDecimal

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String price; // <-- 这里是 String 类型
    private Integer stock;
    private String imageUrl;
    private Long categoryId;
    private String status;

    // --- 构造函数 ---
    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, String description, String price, Integer stock, String imageUrl, Long categoryId, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.status = status;
    }

    // --- Getters 和 Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() { // <-- 获取 String 类型的 price
        return price;
    }

    public void setPrice(String price) { // <-- 设置 String 类型的 price
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", price='" + price + '\'' + // 注意这里打印的是字符串
               ", stock=" + stock +
               ", imageUrl='" + imageUrl + '\'' +
               ", categoryId=" + categoryId +
               ", status='" + status + '\'' +
               '}';
    }
}
