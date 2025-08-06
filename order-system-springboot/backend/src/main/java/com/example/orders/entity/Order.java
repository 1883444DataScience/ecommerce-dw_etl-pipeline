package com.example.orders.entity;

import com.example.orders.common.OrderStatus; // <-- 添加这一行，如果你的OrderStatus在这个包下
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // 如果需要全参构造函数
import javax.persistence.*; // 或 jakarta.persistence.*

// import jakarta.persistence.*; // 如果这是 JPA 实体
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList; // 好的做法是初始化集合

@Entity
@Table(name = "orders") // 数据库中的表名
@Data
@NoArgsConstructor // 如果没有手动写构造函数，可以加上这两个
@AllArgsConstructor // (exclude = {"items"}) // 如果有集合字段，exclude 它们以免生成复杂的构造函数
public class Order { // 注意：这个 Order Entity 现在包含了订单项的属性
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 表的自增主键

    private String orderId; // 对应数据库的 order_id (VARCHAR类型，即订单的唯一业务ID)
    private String userId;  // 对应数据库的 user_id (VARCHAR类型，如果你数据库设计如此)
    private String productId; // 对应数据库的 product_id (VARCHAR类型)
    private Integer quantity;
    private BigDecimal unitPrice; // 单个商品的购买价格
    private BigDecimal totalAmount; // 某个 order_id 下此商品的总金额 (unit_price * quantity)

    @Enumerated(EnumType.STRING) // 你的枚举映射策略
    @Column(name = "order_status") // <-- 明确指定映射到数据库的 'order_status' 列
    private OrderStatus status;
    // private String orderStatus; // 对应 order_status, 可以是 Enum<OrderStatus>
    private LocalDateTime createTime; // 对应 create_time
    private LocalDateTime updateTime; // 对应 update_time

    // 构造函数: no need when lombok is imported
    // public Order() {}

    // // Getters and Setters
    // public Long getId() { return id; }
    // public void setId(Long id) { this.id = id; }
    // public String getOrderId() { return orderId; }
    // public void setOrderId(String orderId) { this.orderId = orderId; }
    // public String getUserId() { return userId; }
    // public void setUserId(String userId) { this.userId = userId; }
    // public String getProductId() { return productId; }
    // public void setProductId(String productId) { this.productId = productId; }
    // public Integer getQuantity() { return quantity; }
    // public void setQuantity(Integer quantity) { this.quantity = quantity; }
    // public BigDecimal getUnitPrice() { return unitPrice; }
    // public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    // public BigDecimal getTotalAmount() { return totalAmount; }
    // public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    // public String getOrderStatus() { return status; }
    // public void setOrderStatus(String orderStatus) { this.status = orderStatus; }
    // public LocalDateTime getCreateTime() { return createTime; }
    // public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    // public LocalDateTime getUpdateTime() { return updateTime; }
    // public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    // // 可以在这里提供一个重载的 setter 方法来接收 String
    // public void setStatus(String statusString) {
    // 	if (statusString != null) {
    //         this.status = OrderStatus.fromString(statusString);
    // 	} else {
    //         this.status = null; // 或者默认值
    //     }
    // }

    // // 如果你的某个DTO或前端需要String类型的状态，你可能需要一个getter来返回String
    // public String getStatusString() {
    //     return this.status != null ? this.status.name() : null; // 或者 .getDescription()
    // }

    @Override
    public String toString() {
        return "Order{" +
               "id=" + id +
               ", orderId='" + orderId + '\'' +
               ", userId='" + userId + '\'' +
               ", productId='" + productId + '\'' +
               ", quantity=" + quantity +
               ", unitPrice=" + unitPrice +
               ", totalAmount=" + totalAmount +
               ", orderStatus='" + status + '\'' +
               ", createTime=" + createTime +
               ", updateTime=" + updateTime +
               '}';
    }
}
