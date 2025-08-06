package com.example.orders.dto;

import java.io.Serializable; // <--- 新增：导入 Serializable 接口
import lombok.Data; // 确保导入 Lombok 的 Data 注解
import lombok.NoArgsConstructor; // 通常需要无参构造函数
import lombok.AllArgsConstructor; // 通常需要全参构造函数

// 如果你用了 @Builder，也要确保相关依赖和配置正确
// import lombok.Builder;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Data // <-- 关键：确保有这个注解，它会自动生成 getter/setter
@NoArgsConstructor // 如果你需要无参构造函数
@AllArgsConstructor // 如果你需要全参构造函数
// @Builder // 如果你使用 Builder 模式来构建 DTO
// 用于下单请求（createOrder）
public class OrderDTO implements Serializable {
    private static final long serialVersionUID = 1L; // 推荐添加 serialVersionUID

    private String orderId; // 订单业务ID，通常也是 String
    private String userId;
    private List<OrderItemDTO> items; // 假设 OrderItemDTO 已经定义
    private String status; // <-- 确保这里有 String 类型的 status 字段
    private java.math.BigDecimal totalAmount;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    // 请勿手动编写 getStatus() 或 setStatus() 方法，让 Lombok 来处理
}
