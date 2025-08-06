package com.example.orders.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter; // 如果你用JSON转换器
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "order.direct.exchange";
    public static final String QUEUE_NAME = "order_queue";
    public static final String ROUTING_KEY = "order.create";

    @Bean
    public Queue orderQueue() {
        // durable: true 表示队列是持久化的，RabbitMQ 重启后队列不会丢失
        return new Queue("order_queue", true);
    }

    // 2. 定义一个交换机 (可选，但推荐使用，特别是当你有多个routing key或更复杂的路由需求时)
    // 这里以 Direct Exchange 为例，最简单，路由键完全匹配
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange("order.direct.exchange", true, false); // durable, autoDelete
    }

    // 3. 定义绑定：将队列绑定到交换机，并指定路由键
    // 确保路由键与你发送消息时使用的 routingKey 匹配
    @Bean
    public Binding binding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with("order.create"); // <--- 定义路由键
    }

    // 如果你使用了 Jackson2JsonMessageConverter，也确保它在这里定义了
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 如果你手动配置了 RabbitTemplate，确保它使用了 MessageConverter
    //@Bean
    //public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
      //  final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //rabbitTemplate.setMessageConverter(jsonMessageConverter); // 设置消息转换器
        // 可以在这里设置默认的 exchange 和 routing key，但通常在 send 方法中指定更灵活
        //return rabbitTemplate;
    //}
}
