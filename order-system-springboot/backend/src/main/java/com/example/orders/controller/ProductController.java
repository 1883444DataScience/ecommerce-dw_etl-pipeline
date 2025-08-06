package com.example.orders.controller;

import com.example.orders.common.ApiResponse;
import com.example.orders.dto.ProductDTO;
import com.example.orders.entity.Product; // 注意：这里返回的是 Product Entity，也可以转换为 ProductDTO
import com.example.orders.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/products") // 所有商品相关API的前缀
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    /**
     * 创建新产品
     * POST /api/products
     * @param productDTO 产品信息
     * @return 创建后的产品信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            Product createdProduct = productService.createProduct(productDTO);
            logger.info("新产品 '{}' (ID: {}) 已创建。", createdProduct.getName(), createdProduct.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdProduct));
        } catch (IllegalArgumentException e) {
            logger.warn("创建产品失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("创建产品时发生内部错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }

    /**
     * 根据ID获取产品
     * GET /api/products/{id}
     * @param id 产品ID
     * @return 产品信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO productDTO = productService.getProductById(id);
        if (productDTO == null) {
            logger.warn("未找到产品，ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "产品不存在。"));
        }
        logger.info("获取产品成功，ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(productDTO));
    }

    /**
     * 获取所有产品
     * GET /api/products
     * @return 所有产品列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        logger.info("获取所有产品成功，共 {} 个。", products.size());
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * 更新产品信息
     * PUT /api/products/{id}
     * @param id 待更新的产品ID
     * @param productDTO 包含更新信息的DTO
     * @return 更新后的产品信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        try {
            productDTO.setId(id); // 确保DTO中的ID与路径ID一致
            Product updatedProduct = productService.updateProduct(productDTO);
            logger.info("产品 '{}' (ID: {}) 信息已更新。", updatedProduct.getName(), updatedProduct.getId());
            return ResponseEntity.ok(ApiResponse.success(updatedProduct));
        } catch (IllegalArgumentException e) {
            logger.warn("更新产品失败，ID: {}. {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("更新产品ID: {} 时发生内部错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }

    /**
     * 删除产品
     * DELETE /api/products/{id}
     * @param id 待删除产品ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        try {
            boolean deleted = productService.deleteProduct(id);
            if (deleted) {
                logger.info("产品 (ID: {}) 已成功删除。", id);
                return ResponseEntity.ok(ApiResponse.success());
            } else {
                logger.warn("尝试删除不存在的产品 (ID: {})。", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "产品不存在或已被删除。"));
            }
        } catch (Exception e) {
            logger.error("删除产品ID: {} 时发生内部错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }
}
