package com.example.orders.mapper;

import com.example.orders.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper // 标记这是一个 MyBatis Mapper 接口
// Mapper 接口定义了对数据库操作的方法。
public interface ProductMapper {

    // 插入一个新商品
    int insertProduct(Product product);

    // 根据ID查找商品
    Product selectProductById(Long id);

    // 查询所有商品
    List<Product> selectAllProducts();

    // 更新商品信息
    int updateProduct(Product product);

    // 根据ID删除商品
    int deleteProductById(Long id);

    // 根据ID更新库存
    // @Param 用于在XML中引用参数名
    int updateProductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // 检查库存是否充足
    Integer checkProductStock(@Param("productId") Long productId);

    // 更多自定义查询，例如：根据分类查询商品
    List<Product> selectProductsByCategoryId(Long categoryId);
}
