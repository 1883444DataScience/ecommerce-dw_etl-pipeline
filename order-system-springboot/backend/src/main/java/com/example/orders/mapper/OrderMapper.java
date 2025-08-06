package com.example.orders.mapper;

import com.example.orders.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    // 插入一条订单项记录
    int insertOrder(Order order); // 实际上是插入一个订单明细行

    // 根据表自增ID查找订单明细行
    Order selectOrderById(Long id);

    // 根据订单业务ID查找所有相关的订单明细行 (一个订单可能有多行)
    List<Order> selectOrdersByOrderId(String orderId);

    // 根据用户ID查找所有相关的订单明细行
    List<Order> selectOrdersByUserId(String userId);

    // 查询所有订单明细行
    List<Order> selectAllOrders();

    // 更新订单明细行信息
    int updateOrder(Order order);

    // 根据表自增ID删除订单明细行
    int deleteOrderById(Long id);

    // 根据订单业务ID删除所有相关的订单明细行 (取消整个订单)
    int deleteOrdersByOrderId(String orderId);

    // 更新特定订单业务ID下的所有订单明细行的状态
    int updateOrderStatusByOrderId(@Param("orderId") String orderId, @Param("status") String status);

    // 更新特定订单业务ID下某个商品的订单状态（如果需要更细粒度控制）
    int updateOrderItemStatus(@Param("orderId") String orderId, @Param("productId") String productId, @Param("status") String status);

    // 获取某个订单（orderId）下的总金额
    BigDecimal calculateTotalAmountByOrderId(String orderId);
}
