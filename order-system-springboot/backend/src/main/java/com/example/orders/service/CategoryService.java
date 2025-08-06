package com.example.orders.service;

import com.example.orders.dto.CategoryDTO;
import com.example.orders.entity.Category;
import com.example.orders.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 创建新分类
     * @param categoryDTO 分类DTO
     * @return 创建后的Category实体
     */
    @Transactional
    public Category createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setParentId(categoryDTO.getParentId());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        categoryMapper.insertCategory(category);
        logger.info("新分类 '{}' (ID: {}) 已创建。", category.getName(), category.getId());
        return category;
    }

    /**
     * 根据ID获取分类
     * @param id 分类ID
     * @return 分类DTO
     */
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryMapper.selectCategoryById(id);
        return category != null ? convertToDto(category) : null;
    }

    /**
     * 获取所有分类
     * @return 分类DTO列表
     */
    public List<CategoryDTO> getAllCategories() {
        return categoryMapper.selectAllCategories().stream()
                       .map(this::convertToDto)
                       .collect(Collectors.toList());
    }

    /**
     * 根据父分类ID获取子分类
     * @param parentId 父分类ID
     * @return 子分类DTO列表
     */
    public List<CategoryDTO> getCategoriesByParentId(Long parentId) {
        return categoryMapper.selectCategoriesByParentId(parentId).stream()
                       .map(this::convertToDto)
                       .collect(Collectors.toList());
    }

    /**
     * 更新分类信息
     * @param categoryDTO 待更新的分类DTO
     * @return 更新后的Category实体
     */
    @Transactional
    public Category updateCategory(CategoryDTO categoryDTO) {
        if (categoryDTO.getId() == null) {
            throw new IllegalArgumentException("更新分类时ID不能为空。");
        }
        Category existingCategory = categoryMapper.selectCategoryById(categoryDTO.getId());
        if (existingCategory == null) {
            throw new IllegalArgumentException("分类不存在，ID: " + categoryDTO.getId());
        }

        if (categoryDTO.getName() != null) existingCategory.setName(categoryDTO.getName());
        if (categoryDTO.getParentId() != null) existingCategory.setParentId(categoryDTO.getParentId());
        existingCategory.setUpdatedAt(LocalDateTime.now());

        categoryMapper.updateCategory(existingCategory);
        logger.info("分类 '{}' (ID: {}) 已更新。", existingCategory.getName(), existingCategory.getId());
        return existingCategory;
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteCategory(Long id) {
        // 实际业务中，可能需要检查该分类下是否有商品，或者是否有子分类，才能删除
        int rowsAffected = categoryMapper.deleteCategoryById(id);
        if (rowsAffected > 0) {
            logger.info("分类 (ID: {}) 已删除。", id);
            return true;
        }
        logger.warn("尝试删除不存在的分类 (ID: {})。", id);
        return false;
    }

    // 辅助方法：Entity 转换为 DTO
    private CategoryDTO convertToDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setParentId(category.getParentId());
        return dto;
    }

    // 辅助方法：DTO 转换为 Entity
    private Category convertToEntity(CategoryDTO dto) {
        Category entity = new Category();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setParentId(dto.getParentId());
        return entity;
    }
}
