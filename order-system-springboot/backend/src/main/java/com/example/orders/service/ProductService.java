package com.example.orders.service;

import com.example.orders.common.ProductStatus; // <-- 确保有这一行
import com.example.orders.dto.ProductDTO;
import com.example.orders.entity.Product;
import com.example.orders.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductMapper productMapper;

    /**
     * 创建新产品
     * @param productDTO 从前端接收的 ProductDTO 对象
     * @return 创建后的 Product Entity
     */
    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
	// the change
	product.setStatus(ProductStatus.fromString(productDTO.getStatus())); // 使用 fromString 方法进行转换
        // 核心转换：String 转 BigDecimal
        try {
            product.setPrice(new BigDecimal(productDTO.getPrice()));
        } catch (NumberFormatException e) {
            logger.error("价格字符串 '{}' 无法转换为有效的 BigDecimal: {}", productDTO.getPrice(), e.getMessage());
            throw new IllegalArgumentException("无效的价格格式: " + productDTO.getPrice());
        }

        product.setStock(productDTO.getStock());
        product.setImageUrl(productDTO.getImageUrl());
        product.setCategoryId(productDTO.getCategoryId());
        
	// product.setStatus(...)期望的是一个 ProductStatus 类型
	product.setStatus(ProductStatus.valueOf(productDTO.getStatus()));
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productMapper.insertProduct(product);
        logger.info("新产品已保存: {}", product.getName());
        return product;
    }

    /**
     * 根据ID获取产品，并转换为 ProductDTO 返回给前端
     * @param id 产品ID
     * @return ProductDTO 对象
     */
    public ProductDTO getProductById(Long id) {
        Product product = productMapper.selectProductById(id);
        return product != null ? convertToDto(product) : null;
    }

    /**
     * 获取所有产品，并转换为 ProductDTO 列表返回给前端
     * @return ProductDTO 列表
     */
    public List<ProductDTO> getAllProducts() {
        return productMapper.selectAllProducts().stream()
                       .map(this::convertToDto)
                       .collect(Collectors.toList());
    }

    /**
     * 更新产品信息
     * @param productDTO 待更新的产品DTO
     * @return 更新后的产品Entity
     */
    @Transactional
    public Product updateProduct(ProductDTO productDTO) {
        if (productDTO.getId() == null) {
            throw new IllegalArgumentException("更新产品时ID不能为空。");
        }
        Product existingProduct = productMapper.selectProductById(productDTO.getId());
        if (existingProduct == null) {
            throw new IllegalArgumentException("产品不存在，ID: " + productDTO.getId());
        }

        if (productDTO.getName() != null) existingProduct.setName(productDTO.getName());
        if (productDTO.getDescription() != null) existingProduct.setDescription(productDTO.getDescription());

        // 核心转换：String 转 BigDecimal (更新时也需要)
        if (productDTO.getPrice() != null && !productDTO.getPrice().isEmpty()) {
            try {
                existingProduct.setPrice(new BigDecimal(productDTO.getPrice()));
            } catch (NumberFormatException e) {
                logger.error("更新产品ID: {}，价格字符串 '{}' 无法转换为有效的 BigDecimal: {}", productDTO.getId(), productDTO.getPrice(), e.getMessage());
                throw new IllegalArgumentException("无效的价格格式: " + productDTO.getPrice());
            }
        }

        if (productDTO.getStock() != null) existingProduct.setStock(productDTO.getStock());
        if (productDTO.getImageUrl() != null) existingProduct.setImageUrl(productDTO.getImageUrl());
        if (productDTO.getCategoryId() != null) existingProduct.setCategoryId(productDTO.getCategoryId());
        if (productDTO.getStatus() != null) {
	    try {
                ProductStatus status = ProductStatus.valueOf(productDTO.getStatus()); // 字符串转枚举
                existingProduct.setStatus(status);
    	    } catch (IllegalArgumentException e) {
                // 可选：抛异常或记录日志，说明状态无效
                throw new RuntimeException("无效的产品状态: " + productDTO.getStatus());
            }	
	}
	// existingProduct.setStatus(productDTO.getStatus());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        productMapper.updateProduct(existingProduct);
        logger.info("产品ID: {} 已更新: {}", existingProduct.getId(), existingProduct.getName());
        return existingProduct;
    }

    /**
     * 删除产品
     * @param id 产品ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteProduct(Long id) {
        int rowsAffected = productMapper.deleteProductById(id);
        if (rowsAffected > 0) {
            logger.info("产品 (ID: {}) 已删除。", id);
            return true;
        }
        logger.warn("尝试删除不存在的产品 (ID: {})。", id);
        return false;
    }

    /**
     * 扣减产品库存
     * @param productId 产品ID
     * @param quantity 扣减数量
     * @return 实际扣减后的库存量 (如果返回-1表示库存不足或产品不存在)
     */
    @Transactional
    public int decreaseStock(Long productId, int quantity) {
        Integer currentStock = productMapper.checkProductStock(productId);
        if (currentStock == null) {
            logger.warn("尝试扣减不存在的产品ID: {} 的库存。", productId);
            return -1; // 产品不存在
        }
        if (currentStock < quantity) {
            logger.warn("产品ID: {} 库存不足，当前库存: {}，尝试扣减: {}", productId, currentStock, quantity);
            return -1; // 库存不足
        }
        int rowsAffected = productMapper.updateProductStock(productId, -quantity); // 扣减用负数
        if (rowsAffected > 0) {
            logger.info("产品ID: {} 库存已扣减 {}，当前库存: {}", productId, quantity, currentStock - quantity);
            return currentStock - quantity;
        }
        logger.error("扣减产品ID: {} 库存失败，可能存在并发问题。", productId);
        throw new RuntimeException("库存扣减失败，请重试。"); // 通常是并发更新失败
    }

    /**
     * 增加产品库存
     * @param productId 产品ID
     * @param quantity 增加数量
     * @return 实际增加后的库存量
     */
    @Transactional
    public int increaseStock(Long productId, int quantity) {
        int rowsAffected = productMapper.updateProductStock(productId, quantity);
        if (rowsAffected > 0) {
            logger.info("产品ID: {} 库存已增加 {}。", productId, quantity);
            // 这里可以再次查询库存返回，或者根据业务逻辑直接返回成功
            return productMapper.checkProductStock(productId);
        }
        logger.error("增加产品ID: {} 库存失败。", productId);
        throw new RuntimeException("库存增加失败。");
    }

    // 辅助方法：Entity 转换为 DTO
    private ProductDTO convertToDto(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice() != null ? product.getPrice().toPlainString() : null);
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategoryId());
        dto.setStatus(product.getStatus().name());
        return dto;
    }

    // 辅助方法：DTO 转换为 Entity (这里主要用于插入，更新时通常加载现有Entity)
    private Product convertToEntity(ProductDTO dto) {
        Product entity = new Product();
        entity.setId(dto.getId()); // 如果DTO包含ID，用于更新
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        if (dto.getPrice() != null && !dto.getPrice().isEmpty()) {
            entity.setPrice(new BigDecimal(dto.getPrice()));
        }
        entity.setStock(dto.getStock());
        entity.setImageUrl(dto.getImageUrl());
        entity.setCategoryId(dto.getCategoryId());
        // entity.setStatus(dto.getStatus());
	if (dto.getStatus() != null) {
	    try {
        	entity.setStatus(ProductStatus.valueOf(dto.getStatus()));
    	    } catch (IllegalArgumentException e) {
        	throw new RuntimeException("无效的产品状态: " + dto.getStatus());
    	    }
	}

        return entity;
    }
}
