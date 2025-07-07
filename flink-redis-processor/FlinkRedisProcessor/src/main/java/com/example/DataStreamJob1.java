package com.example;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

public class DataStreamJob1 {
    public static void main(String[] args) throws Exception {
        // 1. 创建 Flink 执行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. 配置 RabbitMQ 连接
        RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
            .setHost("localhost")  // RabbitMQ 服务器地址
            .setPort(5672)        // 默认端口
            .setUserName("guest")
            .setPassword("guest")
            .setVirtualHost("/")
            .build();

        // 3. 创建 RabbitMQ 数据源（消费指定队列）
        RMQSource<String> source = new RMQSource<> (
                connectionConfig,      // RabbitMQ 连接配置
                "flink-input-queue",  // 要消费的队列名
                true,                 // 自动确认消息
                new SimpleStringSchema() // 数据反序列化方式
        );
	
	// 注册source
	DataStream<String> rabbitMQStream = env.addSource(source);
        // 4. 数据处理（示例：单词计数）
        DataStream<Tuple2<String, Integer>> processedStream = rabbitMQStream
            .flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                @Override
                public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
                    // 按空格拆分单词，每个单词计数1
                    for (String word : value.split("\\s")) {
                        out.collect(new Tuple2<>(word, 1));
                    }
                }
            })
            .returns(Types.TUPLE(Types.STRING, Types.INT)) // 指定返回类型
            .keyBy(0)  // 按单词分组
            .sum(1);   // 累加计数

        processedStream.print();

	// 5. 配置 Redis 连接
        FlinkJedisPoolConfig redisConfig = new FlinkJedisPoolConfig.Builder()
            .setHost("localhost")  // Redis 服务器地址
            .setPort(6379)        // 默认端口
	    .setPassword("093390Aa")
            .build();

        // 6. 创建 Redis Sink（将结果写入Redis的Hash结构）
        processedStream
//	    .map(t -> {
  //      	System.out.println("Writing to Redis: " + t);
    //     	return t;
    //	    })
      //      .returns(Types.TUPLE(Types.STRING, Types.INT)) 	
	    .addSink(new RedisSink<>(
                redisConfig,
                new RedisWordCountMapper()
            ));

        // 7. 打印处理结果到控制台（调试用）
        processedStream.print();

        // 8. 执行任务
        env.execute("Flink RabbitMQ to Redis Job");
    }

    // 自定义Redis映射器（定义如何存储数据到Redis）
    public static class RedisWordCountMapper implements RedisMapper<Tuple2<String, Integer>> {
        @Override
        public RedisCommandDescription getCommandDescription() {
            // 使用HSET命令，存储到名为"word_counts"的Hash中
            return new RedisCommandDescription(RedisCommand.HSET, "word_counts");
        }

        @Override	
        public String getKeyFromData(Tuple2<String, Integer> data) {
            // Hash的field = 单词
            return data.f0;
        }

        @Override
        public String getValueFromData(Tuple2<String, Integer> data) {
            // Hash的value = 计数
            return data.f1.toString();
        }
    }
}
