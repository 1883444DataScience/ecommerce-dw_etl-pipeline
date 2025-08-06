package com.example.orders.service;

import com.example.orders.dto.OrderDetailDTO;
import com.example.orders.dto.OrderItemDTO;
import com.example.orders.common.OrderStatus;
import com.example.orders.dto.OrderDTO;
import com.example.orders.dto.OrderItemDetailDTO;
import com.example.orders.entity.Order; // 这是扁平化的Order Entity
import com.example.orders.entity.Product; // 用于查询商品信息
import com.example.orders.mapper.OrderMapper;
import com.example.orders.mapper.ProductMapper; // 用于商品库存操作
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID; // 用于生成订单业务ID
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper; // 用于检查和扣减库存

    /**
     * 创建订单。
     * 由于orders表是扁平化的，一个OrderDTO会生成多条Order实体记录。
     * 在高并发场景下，这里需要考虑库存锁和事务。
     *
     * @param orderDTO 包含订单信息的DTO，其中items是商品列表
     * @return 创建成功的订单业务ID
     */
    @Transactional // 确保订单创建和库存扣减的原子性
    public String createOrder(OrderDTO orderDTO) {
        // 1. 生成唯一的订单业务ID
        String orderBusinessId = UUID.randomUUID().toString();
        orderDTO.setOrderId(orderBusinessId); // 设置到DTO中，方便后续处理

        // 2. 校验和预扣减库存
        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单商品列表不能为空。");
        }

        // 计算总金额并进行库存检查和预扣减
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        List<Order> ordersToInsert = new ArrayList<>();
	
	// orderDTO.getItems() 返回的是List<OrderItemDTO> 而不是List<OrderItemDetailDTO>
        for (OrderItemDTO itemDTO : orderDTO.getItems()) {
            // 获取商品信息，检查是否存在和价格
            // 不需要创建: OrderItemDetailDTO detailDTO = new OrderItemDetailDTO(); // 创建新的 DetailDTO
	    //     detailDTO.setProductId(itemDTO.getProductId());
	    //     detailDTO.setQuantity(itemDTO.getQuantity());
	    Product product = productMapper.selectProductById(Long.valueOf(itemDTO.getProductId()));
            if (product == null) {
                throw new IllegalArgumentException("商品不存在，ID: " + itemDTO.getProductId());
            }
            if (itemDTO.getQuantity() <= 0) {
                throw new IllegalArgumentException("商品数量必须大于0，ID: " + itemDTO.getProductId());
            }

            // DTO中的unitPrice是String，这里转换成BigDecimal
            BigDecimal itemUnitPrice;
            try {
                itemUnitPrice = new BigDecimal(itemDTO.getUnitPrice());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("商品价格格式无效: " + itemDTO.getUnitPrice());
            }

            // 校验前端传递的单价是否与数据库一致（防止篡改）
            if (product.getPrice().compareTo(itemUnitPrice) != 0) {
                 logger.warn("订单商品 {} 单价不匹配，前端: {}，数据库: {}", itemDTO.getProductId(), itemUnitPrice, product.getPrice());
                 // 强制使用数据库价格 或 抛出异常
                 itemUnitPrice = product.getPrice(); // 强制使用数据库中的价格
                 // throw new IllegalArgumentException("商品价格不匹配: " + itemDTO.getProductId());
            }

            // 尝试扣减库存
            // 注意：这里需要更高级的并发控制，比如数据库行锁，或者在 ProductService.decreaseStock 内部实现乐观锁/悲观锁
            int remainingStock = productMapper.updateProductStock(product.getId(), -itemDTO.getQuantity());
            if (remainingStock < 0) { // updateProductStock 返回的是 affected rows，不是剩余库存
                // 重新查询库存以判断是否不足
                Integer currentStock = productMapper.checkProductStock(product.getId());
                if (currentStock == null || currentStock < itemDTO.getQuantity()) {
                    throw new RuntimeException("商品 " + product.getName() + " (ID: " + product.getId() + ") 库存不足，请重新下单。");
                }
                // 如果是乐观锁，这里可能捕获到 StaleObjectStateException 或类似的异常
                // 暂时用简单的判断，实际在高并发下需要更严格的锁
            }

            // 计算当前订单项的总金额
            BigDecimal itemTotalAmount = itemUnitPrice.multiply(new BigDecimal(itemDTO.getQuantity()));
            totalOrderAmount = totalOrderAmount.add(itemTotalAmount);

            // 构建 Order 实体（代表订单中的一个商品记录）
            Order orderLine = new Order();
            orderLine.setOrderId(orderBusinessId); // 关联同一个订单业务ID
            orderLine.setUserId(String.valueOf(orderDTO.getUserId())); // 关联用户
            orderLine.setProductId(String.valueOf(itemDTO.getProductId()));
            orderLine.setQuantity(itemDTO.getQuantity());
            orderLine.setUnitPrice(itemUnitPrice);
            // orderLine.setTotalAmount(itemTotalAmount);
	    orderLine.setTotalAmount(itemUnitPrice.multiply(new BigDecimal(itemDTO.getQuantity())));
            // orderLine.setStatus("NEW"); // 新订单状态，等待支付等
	    // 将 DTO 中的 String status 转换为 OrderStatus 枚举
    	    if (orderDTO.getStatus() != null && !orderDTO.getStatus().trim().isEmpty()) {
         	orderLine.setStatus(OrderStatus.fromString(orderDTO.getStatus()));		
    	    } else {
            	// 根据业务需求处理 null 值，例如设置为默认状态
       		orderLine.setStatus(OrderStatus.PENDING);
	    }            

	    orderLine.setCreateTime(LocalDateTime.now());
            orderLine.setUpdateTime(LocalDateTime.now());
            ordersToInsert.add(orderLine);
        }

        // 3. 批量插入订单明细记录 (MyBatis 通常不直接支持批量插入，需要循环或使用 foreach 标签)
        for (Order orderLine : ordersToInsert) {
            orderMapper.insertOrder(orderLine);
        }

        // logger.info("订单 (业务ID: {}) 已创建，包含 {} 个商品项，总金额: {}",
           //         orderBusinessId, orderDTO.getItems().size(), totalOrderAmount);

        // 如果需要，可以在这里计算并更新 orders 表中总金额的字段（但你的 orders 表每行一个商品，没有总金额字段）
        // 如果你需要一个代表整个订单的总金额，你需要考虑如何存储它
        // 目前你的 orders 表的 total_amount 是针对该行 product_id * quantity 的金额

        return orderBusinessId; // 返回订单业务ID
    }

    /**
     * 根据订单业务ID获取订单详情。
     * 由于一个订单对应多条orders表记录，这里会聚合为一个 OrderDTO。
     *
     * @param orderId 订单业务ID
     * @return 聚合后的 OrderDTO
     */
    public OrderDetailDTO getOrderByOrderId(String orderId) {
        List<Order> orderLines = orderMapper.selectOrdersByOrderId(orderId);
        if (orderLines == null || orderLines.isEmpty()) {
            return null;
        }

        // 聚合为 OrderDTO
        OrderDetailDTO orderDTO = new OrderDetailDTO();
        orderDTO.setOrderId(orderId);
        orderDTO.setUserId(String.valueOf(orderLines.get(0).getUserId())); // 订单的userId对所有行都一样

        List<OrderItemDetailDTO> itemDetails = orderLines.stream()
                .map(this::convertOrderItemToDetailDTO) // 转换每一行为订单明细DTO
                .collect(Collectors.toList());
        orderDTO.setItems(itemDetails);

        return orderDTO;
    }

    /**
     * 根据用户ID获取所有订单（每个订单聚合为一个 OrderDTO）。
     *
     * @param userId 用户ID
     * @return 用户所有订单的列表
     */
    public List<OrderDTO> getOrdersByUserId(String userId) {
        List<Order> allOrderLines = orderMapper.selectOrdersByUserId(userId);
        if (allOrderLines == null || allOrderLines.isEmpty()) {
            return new ArrayList<>();
        }

        // 将扁平化的订单行按 order_id 分组
        Map<String, List<Order>> ordersGroupedById = allOrderLines.stream()
                .collect(Collectors.groupingBy(Order::getOrderId));

        // 将每个订单组聚合为一个 OrderDTO
        return ordersGroupedById.entrySet().stream()
                .map(entry -> {
                    String orderId = entry.getKey();
                    List<Order> lines = entry.getValue();
                    OrderDTO dto = new OrderDTO();
                    dto.setOrderId(String.valueOf(orderId));
                    // dto.setUserId(Long.parseLong(lines.get(0).getUserId())); // 假定所有行的userId相同
		    dto.setUserId(lines.get(0).getUserId());
                    List<OrderItemDetailDTO> itemDetails = lines.stream()
                            .map(this::convertOrderItemToDetailDTO)
                            .collect(Collectors.toList());
                    // List<OrderItemDTO> baseItems = itemDetails.stream()
        		//.map(item -> (OrderItemDTO) item)
        		//.collect(Collectors.toList());
		    List<OrderItemDTO> baseItems = itemDetails.stream().map(detail -> {
    			OrderItemDTO itemDTO = new OrderItemDTO();
    			itemDTO.setProductId(Long.valueOf(detail.getProductId())); // getProductId() 返回的是 String，但 setProductId() 需要的是 Long
    			itemDTO.setQuantity(detail.getQuantity());
    			itemDTO.setUnitPrice(detail.getUnitPrice());
    			// 其他字段
    			return itemDTO;
		    }).collect(Collectors.toList());
		    
		    dto.setItems(baseItems);	
                    return dto;
                })
                .sorted((o1, o2) -> { // 根据订单号排序，或者你可以根据其他字段排序，例如创建时间（如果能获取到）
                    // 订单聚合后，无法直接从 OrderDTO 获取 createTime，需要从第一个 item 或单独查询
                    // 这里为了简化，假设 orderId 某种程度上有序或无序也可
                    return o2.getOrderId().compareTo(o1.getOrderId());
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新订单状态 (影响某个订单业务ID下的所有订单明细行)
     * @param orderId 订单业务ID
     * @param newStatus 新状态
     * @return 更新的行数
     */
    @Transactional
    public int updateOrderStatus(String orderId, String newStatus) {
        int rowsAffected = orderMapper.updateOrderStatusByOrderId(orderId, newStatus);
        logger.info("订单 (业务ID: {}) 状态更新为: {}，影响 {} 条记录。", orderId, newStatus, rowsAffected);
        return rowsAffected;
    }

    /**
     * 取消订单 (删除某个订单业务ID下的所有订单明细行)
     * 在实际应用中，取消订单通常是更新状态而不是物理删除
     *
     * @param orderId 订单业务ID
     * @return 是否成功删除
     */
    @Transactional
    public boolean cancelOrder(String orderId) {
        // TODO: 在取消订单前，可能需要将库存回滚
        List<Order> orderItemsToRollback = orderMapper.selectOrdersByOrderId(orderId);
        if (orderItemsToRollback != null && !orderItemsToRollback.isEmpty()) {
            for (Order item : orderItemsToRollback) {
                // 回滚库存
                productMapper.updateProductStock(Long.valueOf(item.getProductId()), item.getQuantity());
                logger.info("订单取消：产品ID {} 库存回滚 {}。", item.getProductId(), item.getQuantity());
            }
        }

        int rowsAffected = orderMapper.deleteOrdersByOrderId(orderId);
        if (rowsAffected > 0) {
            logger.info("订单 (业务ID: {}) 已取消（删除），影响 {} 条记录。", orderId, rowsAffected);
            return true;
        }
        logger.warn("尝试取消不存在的订单 (业务ID: {})。", orderId);
        return false;
    }

    // 辅助方法：将 Order Entity (扁平化行) 转换为 OrderItemDetailDTO
    private OrderItemDetailDTO convertOrderItemToDetailDTO(Order order) {
        OrderItemDetailDTO dto = new OrderItemDetailDTO();
        dto.setProductId(order.getProductId());
        dto.setQuantity(order.getQuantity());
        dto.setUnitPrice(order.getUnitPrice() != null ? order.getUnitPrice().toPlainString() : null);
        return dto;
    }
}
