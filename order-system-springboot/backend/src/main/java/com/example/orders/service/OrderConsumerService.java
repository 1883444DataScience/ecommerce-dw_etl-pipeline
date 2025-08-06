package com.example.orders.service;

import com.example.orders.common.OrderStatus; // 确保导入 OrderStatus 枚举
import com.example.orders.dto.OrderDTO;
import com.example.orders.dto.OrderItemDTO;
import com.example.orders.entity.Order;
import com.example.orders.repository.OrderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID; // Used for generating orderId

@Service
public class OrderConsumerService {

    private final OrderRepository orderRepository;

    public OrderConsumerService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @RabbitListener(queues = "order_queue")
    public void processOrder(OrderDTO orderDTO) {
        System.out.println("从 RabbitMQ 接收到订单消息: " + orderDTO.getOrderId());
        try {
            // Generate or retrieve the unique business orderId (this ID will be used for all order items)
            String commonOrderId = orderDTO.getOrderId();
            if (commonOrderId == null || commonOrderId.isEmpty()) {
                commonOrderId = UUID.randomUUID().toString();
                System.out.println("为订单生成新的业务ID: " + commonOrderId);
            }

            // Check if OrderDTO contains order items
            if (orderDTO.getItems() != null && !orderDTO.getItems().isEmpty()) {
                for (OrderItemDTO itemDTO : orderDTO.getItems()) {
                    Order orderEntity = new Order(); // Create a new Order entity for each OrderItemDTO

                    // --- 设置订单的公共属性（类型转换） ---

                    orderEntity.setOrderId(commonOrderId); // String to String

                    // OrderDTO.userId (Long) -> Order.userId (String)
                    if (orderDTO.getUserId() != null) {
                        orderEntity.setUserId(orderDTO.getUserId());
                    } else {
                        orderEntity.setUserId(null); // 或者设置一个默认值，取决于业务需求
                    }


                    // OrderDTO.status (String) -> Order.status (OrderStatus enum)
                    if (orderDTO.getStatus() != null && !orderDTO.getStatus().isEmpty()) {
                        try {
                            orderEntity.setStatus(OrderStatus.valueOf(orderDTO.getStatus().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.err.println("警告: 无法将状态字符串 '" + orderDTO.getStatus() + "' 转换为 OrderStatus 枚举。订单ID: " + commonOrderId);
                            orderEntity.setStatus(OrderStatus.UNKNOWN); // 设置一个默认的未知状态，或抛出异常
                        }
                    } else {
                        orderEntity.setStatus(OrderStatus.UNKNOWN); // 或者设置默认状态
                    }


                    orderEntity.setCreateTime(LocalDateTime.now());
                    orderEntity.setUpdateTime(LocalDateTime.now());

                    // --- 设置订单项的特定属性（类型转换） ---

                    // OrderItemDTO.productId (Long) -> Order.productId (String)
                    if (itemDTO.getProductId() != null) {
                        orderEntity.setProductId(String.valueOf(itemDTO.getProductId()));
                    } else {
                        orderEntity.setProductId(null); // 或者设置默认值
                    }

                    orderEntity.setQuantity(itemDTO.getQuantity()); // Integer to Integer

                    // OrderItemDTO.unitPrice (String) -> Order.unitPrice (BigDecimal)
                    if (itemDTO.getUnitPrice() != null && !itemDTO.getUnitPrice().isEmpty()) {
                        try {
                            orderEntity.setUnitPrice(new BigDecimal(itemDTO.getUnitPrice()));
                        } catch (NumberFormatException e) {
                            System.err.println("警告: 订单项ProductId=" + itemDTO.getProductId() + "的单价字符串 '" + itemDTO.getUnitPrice() + "' 无法转换为 BigDecimal。设为0。");
                            orderEntity.setUnitPrice(BigDecimal.ZERO); // 转换失败设为0
                        }
                    } else {
                        orderEntity.setUnitPrice(BigDecimal.ZERO); // 为空设为0
                    }

                    // --- 计算并设置订单项的总金额 ---
                    // 这里仍然使用 OrderEntity 的 unitPrice 和 quantity 来计算 totalAmount，
                    // 避免 OrderItemDTO 的 totalPrice 字段可能存在的冗余或不一致。
                    if (orderEntity.getUnitPrice() != null && orderEntity.getQuantity() != null) {
                        BigDecimal itemTotal = orderEntity.getUnitPrice().multiply(BigDecimal.valueOf(orderEntity.getQuantity()));
                        orderEntity.setTotalAmount(itemTotal);
                    } else {
                        orderEntity.setTotalAmount(BigDecimal.ZERO);
                        System.err.println("警告: 订单项ProductId=" + orderEntity.getProductId() + "的单价或数量为空，总金额设为0。");
                    }

                    // 保存这个订单项实体到数据库
                    orderRepository.save(orderEntity);
                    System.out.println("已保存订单项: OrderID=" + orderEntity.getOrderId() + ", ProductID=" + orderEntity.getProductId() + ", Quantity=" + orderEntity.getQuantity());
                }
                System.out.println("订单 " + commonOrderId + " (共 " + orderDTO.getItems().size() + " 个订单项) 已成功保存到数据库。");

            } else {
                System.out.println("订单 " + commonOrderId + " 没有包含有效的订单项，无需保存到数据库。");
            }

        } catch (Exception e) {
            System.err.println("处理订单 " + orderDTO.getOrderId() + " 失败，错误: " + e.getMessage());
            e.printStackTrace(); // 打印完整堆栈跟踪，便于调试
        }
    }
}
