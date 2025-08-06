 package com.example.orders.controller;

import com.example.orders.service.OrderService;
import com.example.orders.dto.OrderDetailDTO;
import com.example.orders.common.ApiResponse;
import com.example.orders.dto.OrderDTO;
import com.example.orders.service.OrderProducerService; // 导入 OrderProducerService
// import com.example.orders.service.OrderService; // 如果 OrderService 仅用于数据库操作，并且你想完全异步化，可以注释掉此行或移除其注入

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// 这个 Controller 负责订单的创建、查询和状态更新。
// reminder: 一个逻辑订单在数据库中是多条记录

@RestController
@RequestMapping("/api/orders") // 所有订单相关API的前缀
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    // private OrderService orderService; // 保持原有OrderService注入，用于查询和更新等非创建操作
    private final OrderProducerService orderProducerService; // 新增：注入 OrderProducerService

    // 通过构造器注入 OrderProducerService。如果还需要OrderService用于其他方法，则保持其注入。
    // 这里我们假设 OrderService 的其他方法（查询、更新、取消）仍直接操作数据库
    @Autowired
    public OrderController(OrderProducerService orderProducerService) {
        this.orderProducerService = orderProducerService;
        // 如果还需要 OrderService，则也需要在这里注入
        // this.orderService = orderService;
    }

    // 注意：如果你的 OrderService 实例仍然用于 get/update/cancel 方法，
    // 那么你也需要在这里注入它，例如：
    @Autowired // 如果 OrderService 仍然需要被注入，用于下面的 GET/PUT/DELETE 方法
    private OrderService orderService; // 重新声明注入，或者在构造器中添加

    /**
     * 创建订单。
     * 前端传入一个包含多个商品项的 OrderDTO。后端将为每个商品项在数据库的 `orders` 表中创建一条记录。
     * POST /api/orders
     * @param orderDTO 包含用户ID和订单商品列表的DTO
     * @return 创建成功的订单业务ID
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            // *** 核心修改点：调用 OrderProducerService 发送消息到 RabbitMQ ***
            orderProducerService.sendOrderToQueue(orderDTO);

            logger.info("订单请求已接收，订单数据已发送至 RabbitMQ 进行异步处理。用户ID: {}", orderDTO.getUserId());
            // 返回 202 Accepted 状态码，表示请求已接受，但处理尚未完成。
            // 或者继续使用 201 Created，但响应信息应明确指出是异步处理。
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success("订单请求已接收，正在异步处理中。"));

        } catch (Exception e) {
            logger.error("处理订单请求时发生内部错误: {}", e.getMessage(), e);
            // 返回 500 服务器内部错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<String>serverError("服务器内部错误，订单提交失败。"));
        }
    }

    /**
     * 根据订单业务ID获取订单详情。
     * 由于数据库orders表是扁平化的，此接口会聚合所有相关的订单明细行，并返回一个 OrderDTO。
	     * GET /api/orders/{orderId}
     * @param orderId 订单的业务唯一ID (varchar类型)
     * @return 订单详情DTO
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Object>> getOrderByOrderId(@PathVariable String orderId) {
        OrderDetailDTO orderDetailDTO = orderService.getOrderByOrderId(orderId);
        if (orderDetailDTO == null) {
            logger.warn("未找到订单，业务ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.<Object>fail(HttpStatus.NOT_FOUND.value(), "订单不存在。"));
        }
        logger.info("获取订单成功，业务ID: {}", orderId);
        return ResponseEntity.ok(ApiResponse.<Object>success("获取订单成功", orderDetailDTO)); // 传递实际数据
    }

    /**
     * 根据用户ID获取所有订单列表。
     * 返回的每个 OrderDTO 代表一个逻辑订单，其中包含了该订单的所有商品明细。
     * GET /api/orders/user/{userId}
     * @param userId 用户ID (varchar类型)
     * @return 用户所有订单列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByUserId(@PathVariable String userId) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        logger.info("获取用户 '{}' 的所有订单成功，共 {} 个。", userId, orders.size());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * 更新订单状态。
     * 此操作将更新某个订单业务ID下的所有订单明细行的状态。
     * PUT /api/orders/{orderId}/status
     * @param orderId 订单业务ID
     * @param status 更新后的订单状态 (例如: PAID, SHIPPED, CANCELLED)
     * @return 操作结果
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status) {
        try {
            int rowsAffected = orderService.updateOrderStatus(orderId, status);
            if (rowsAffected > 0) {
                logger.info("订单 (业务ID: {}) 状态已更新为: {}。", orderId, status);
                return ResponseEntity.ok(ApiResponse.success("更新成功"));
            } else {
                logger.warn("更新订单 (业务ID: {}) 状态失败，可能订单不存在。", orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.<String>fail(HttpStatus.NOT_FOUND.value(), "订单不存在或状态无需更新。"));
            }
        } catch (Exception e) {
            logger.error("更新订单 (业务ID: {}) 状态时发生内部错误: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }

    /**
     * 取消订单。
     * 此操作将删除某个订单业务ID下的所有订单明细行，并回滚库存。
     * DELETE /api/orders/{orderId}
     * @param orderId 订单业务ID
     * @return 操作结果
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<String>> cancelOrder(@PathVariable String orderId) {
        try {
            boolean cancelled = orderService.cancelOrder(orderId);
            if (cancelled) {
                logger.info("订单 (业务ID: {}) 已成功取消。", orderId);
                return ResponseEntity.ok(ApiResponse.success("删除成功"));
            } else {
                logger.warn("尝试取消不存在的订单 (业务ID: {})。", orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "订单不存在或已被取消。"));
            }
        } catch (Exception e) {
            logger.error("取消订单 (业务ID: {}) 时发生内部错误: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }
}
