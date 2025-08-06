package com.example.orders.mapper;

import com.example.orders.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper {
    int insertOrderItem(OrderItem orderItem);
    List<OrderItem> selectOrderItemsByOrderId(Long orderId); // 查询某个订单的所有订单项
    OrderItem selectOrderItemById(Long id);
    int updateOrderItem(OrderItem orderItem);
    int deleteOrderItemById(Long id);
    int deleteOrderItemsByOrderId(Long orderId); // 根据订单ID删除所有订单项
}
