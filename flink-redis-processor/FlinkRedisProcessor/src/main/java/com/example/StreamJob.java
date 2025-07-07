package com.example;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
import org.apache.flink.streaming.api.datastream.DataStream; // 导入 DataStream 类

public class StreamJob {

    public static void main(String[] args) throws Exception {
        // 设置执行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 1. 配置 RabbitMQ 连接
        // 请替换为你的 RabbitMQ 主机、端口、用户名和密码
        RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
                .setHost("localhost")
                .setPort(5672)
                .setUserName("guest")
                .setPassword("guest")
                // .setVirtualHost("/") // 如果你有特定的虚拟主机
                .build();

        // 创建 RabbitMQ Source
        DataStream<String> rmqStream = env.addSource(new RMQSource<String>(
                connectionConfig,
                "my_queue", // 与 Node.js 后端和前端使用的队列名称一致
                new SimpleStringSchema()))
                .name("RabbitMQ Source");

        // 打印接收到的消息到 Flink TaskManager 日志，方便调试
        rmqStream.print().name("Debug Print Sink"); // 将 print() 作为一个独立的 Sink 操作

        // 将数据发送到 Redis Sink
        rmqStream.addSink(new RedisSink<>(
                        createFlinkJedisPoolConfig(),
                        new MyRedisMapper()))
                .name("Redis Sink");

        // 执行 Flink Job
        env.execute("RabbitMQ to Redis Flink Job");
    }

    // 2. 配置 Redis 连接
    private static FlinkJedisPoolConfig createFlinkJedisPoolConfig() {
        // 请替换为你的 Redis 主机、端口和密码
        return new FlinkJedisPoolConfig.Builder()
                .setHost("localhost")
                .setPort(6379)
                // 如果 Redis 设置了密码，请取消注释并填写密码
                // .setPassword("your_redis_password")
                .build();
    }

    // 3. 定义 Redis Mapper: 告诉 Flink 如何将数据写入 Redis
    public static class MyRedisMapper implements RedisMapper<String> {

        @Override
        public RedisCommandDescription getCommandDescription() {
            return new RedisCommandDescription(RedisCommand.SET);
        }

        @Override
        public String getKeyFromData(String data) {
            return "message_key:" + data;
        }

        @Override
        public String getValueFromData(String data) {
            return data + "_processed_at_" + System.currentTimeMillis();
        }
    }
}
