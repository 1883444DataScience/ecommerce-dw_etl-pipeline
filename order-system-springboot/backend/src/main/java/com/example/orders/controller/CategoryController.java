package com.example.orders.controller;

import com.example.orders.common.ApiResponse;
import com.example.orders.dto.CategoryDTO;
import com.example.orders.entity.Category;
import com.example.orders.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/categories") // 所有分类相关API的前缀
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    /**
     * 创建新分类
     * POST /api/categories
     * @param categoryDTO 分类信息
     * @return 创建后的分类信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody CategoryDTO categoryDTO) {
        try {
            Category createdCategory = categoryService.createCategory(categoryDTO);
            logger.info("新分类 '{}' (ID: {}) 已创建。", createdCategory.getName(), createdCategory.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdCategory));
        } catch (IllegalArgumentException e) {
            logger.warn("创建分类失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("创建分类时发生内部错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }

    /**
     * 根据ID获取分类
     * GET /api/categories/{id}
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        CategoryDTO categoryDTO = categoryService.getCategoryById(id);
        if (categoryDTO == null) {
            logger.warn("未找到分类，ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "分类不存在。"));
        }
        logger.info("获取分类成功，ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(categoryDTO));
    }

    /**
     * 获取所有分类
     * GET /api/categories
     * @return 所有分类列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        logger.info("获取所有分类成功，共 {} 个。", categories.size());
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 根据父分类ID获取子分类
     * GET /api/categories/parent/{parentId}
     * @param parentId 父分类ID (如果为0或null表示获取顶级分类)
     * @return 子分类列表
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<CategoryDTO> categories = categoryService.getCategoriesByParentId(parentId);
        logger.info("获取父分类ID {} 下的子分类成功，共 {} 个。", parentId, categories.size());
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 更新分类信息
     * PUT /api/categories/{id}
     * @param id 待更新的分类ID
     * @param categoryDTO 包含更新信息的DTO
     * @return 更新后的分类信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        try {
            categoryDTO.setId(id); // 确保DTO中的ID与路径ID一致
            Category updatedCategory = categoryService.updateCategory(categoryDTO);
            logger.info("分类 '{}' (ID: {}) 信息已更新。", updatedCategory.getName(), updatedCategory.getId());
            return ResponseEntity.ok(ApiResponse.success(updatedCategory));
        } catch (IllegalArgumentException e) {
            logger.warn("更新分类失败，ID: {}. {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("更新分类ID: {} 时发生内部错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }

    /**
     * 删除分类
     * DELETE /api/categories/{id}
     * @param id 待删除分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        try {
            boolean deleted = categoryService.deleteCategory(id);
            if (deleted) {
                logger.info("分类 (ID: {}) 已成功删除。", id);
                return ResponseEntity.ok(ApiResponse.success());
            } else {
                logger.warn("尝试删除不存在的分类 (ID: {})。", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "分类不存在或已被删除。"));
            }
        } catch (Exception e) {
            logger.error("删除分类ID: {} 时发生内部错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }
}
