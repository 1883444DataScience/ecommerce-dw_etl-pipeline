package com.example.orders.service;

import com.example.orders.dto.OrderDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class OrderProducerService {

    private final RabbitTemplate rabbitTemplate;
    
    // 注入 RabbitTemplate
    @Autowired
    public OrderProducerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
	
    // 定义常量以避免硬编码和潜在的拼写错误
    // 确保这些常量的值与 RabbitMQConfig 中定义的完全一致
    public static final String ORDER_EXCHANGE_NAME = "order.direct.exchange"; // 你的交换机名称
    public static final String ORDER_ROUTING_KEY = "order.create"; // 你的路由键

    // 明确指定要发送到的交换机和路由键。
    public void sendOrderToQueue(OrderDTO orderDTO) {
        // "order.direct.exchange" 是交换机名称
        // "order.direct.routingkey" 是路由键
	System.out.println("DEBUG: 准备发送订单消息到 Exchange: " + ORDER_EXCHANGE_NAME + ", Routing Key: " + ORDER_ROUTING_KEY + ", DTO: " + orderDTO);
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE_NAME, ORDER_ROUTING_KEY, orderDTO);
        System.out.println("订单消息已发送到 RabbitMQ: " + orderDTO.getOrderId());
    }
}
