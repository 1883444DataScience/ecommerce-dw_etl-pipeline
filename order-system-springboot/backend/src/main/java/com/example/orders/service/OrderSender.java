package com.example.orders.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // You'll also need LoggerFactory to initialize it

import com.example.orders.config.RabbitMQConfig;
import com.example.orders.dto.OrderDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderSender {

    private static final Logger logger = LoggerFactory.getLogger(OrderSender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;  // 用于序列化JSON

    public void sendOrder(OrderDTO order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
	    // !! 在这里打印出完整的 JSON 字符串，检查 unitPrice 是否带引号 !!
            logger.info("准备发送到 RabbitMQ 的 JSON 消息: {}", orderJson);

            rabbitTemplate.convertAndSend("order_queue", orderJson);
	    logger.info("订单消息已发送到 RabbitMQ");

        } catch (JsonProcessingException e) {
            logger.error("将 OrderDTO 转换为 JSON 失败: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("发送订单消息到 RabbitMQ 失败: {}", e.getMessage(), e);
        }
    }
}
