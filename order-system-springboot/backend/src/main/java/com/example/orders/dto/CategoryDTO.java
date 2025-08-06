package com.example.orders.dto;

public class CategoryDTO {
    private Long id;
    private String name;
    private Long parentId;

    // 构造函数
    public CategoryDTO() {}

    public CategoryDTO(Long id, String name, Long parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    @Override
    public String toString() {
        return "CategoryDTO{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", parentId=" + parentId +
               '}';
    }
}
