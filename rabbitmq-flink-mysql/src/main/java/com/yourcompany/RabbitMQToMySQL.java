package com.yourcompany;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.json.JSONArray;
import org.json.JSONObject;

import com.yourcompany.orders.common.OrderStatus;
import com.yourcompany.orders.entity.Order;
import com.yourcompany.orders.entity.Product;
import com.yourcompany.orders.entity.OrderItem;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class RabbitMQToMySQL {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
                .setHost("localhost")
                .setPort(5672)
                .setUserName("guest")
                .setPassword("guest")
                .setVirtualHost("/")
                .build();

        RMQSource<String> rabbitSource = new RMQSource<>(
                connectionConfig,
                "order_queue",
                false,
                new SimpleStringSchema()
        );

        DataStream<Tuple3<Order, Product, OrderItem>> processedStream = env.addSource(rabbitSource)
                .flatMap(new FlatMapFunction<String, Tuple3<Order, Product, OrderItem>>() {
                    @Override
                    public void flatMap(String jsonMessage, Collector<Tuple3<Order, Product, OrderItem>> collector) throws Exception {
                        System.out.println("🐇 Received raw message from RabbitMQ: " + jsonMessage);
                        JSONObject orderDtoJson = new JSONObject(jsonMessage);

                        // --- 解析 OrderDTO 顶级字段 ---
                        // 订单ID在JSON中是字符串，但在Order实体中也可能是字符串
                        String commonOrderIdString = orderDtoJson.getString("orderId");
                        if (commonOrderIdString == null || commonOrderIdString.isEmpty()) {
                            commonOrderIdString = UUID.randomUUID().toString();
                            System.out.println("Flink: 为订单生成新的业务ID: " + commonOrderIdString);
                        }
                        
                        // 订单ID在 OrderItem 实体中是 Long
                        Long commonOrderIdLong = null;
                        try {
                            if (commonOrderIdString != null && !commonOrderIdString.isEmpty()) {
                                commonOrderIdLong = Long.parseLong(commonOrderIdString);
                            }
                        } catch (NumberFormatException e) {
                             System.err.println("Flink: 无法将订单ID '" + commonOrderIdString + "' 转换为 Long。");
                        }


                        // 用户ID在JSON中是Long，但可能在Order实体中是String
                        Long userId = orderDtoJson.has("userId") && !orderDtoJson.isNull("userId")
                                ? orderDtoJson.getLong("userId") : null;

                        OrderStatus status = OrderStatus.UNKNOWN;
                        if (orderDtoJson.has("status") && !orderDtoJson.isNull("status")) {
                            try {
                                status = OrderStatus.valueOf(orderDtoJson.getString("status").toUpperCase());
                            } catch (IllegalArgumentException e) {
                                System.err.println("Flink: 无法将状态字符串 '" + orderDtoJson.getString("status") + "' 转换为 OrderStatus 枚举。设为UNKNOWN。");
                            }
                        }

                        ZoneId cstZoneId = ZoneId.of("Asia/Shanghai");
                        LocalDateTime createTime, updateTime;
                        
                        ZonedDateTime zonedCreateTime = orderDtoJson.has("createdAt") && !orderDtoJson.isNull("createdAt")
                            ? ZonedDateTime.parse(orderDtoJson.getString("createdAt"), DateTimeFormatter.ISO_DATE_TIME)
                            : ZonedDateTime.now(cstZoneId);
                        createTime = zonedCreateTime.withZoneSameInstant(cstZoneId).toLocalDateTime();

                        ZonedDateTime zonedUpdateTime = orderDtoJson.has("updatedAt") && !orderDtoJson.isNull("updatedAt")
                            ? ZonedDateTime.parse(orderDtoJson.getString("updatedAt"), DateTimeFormatter.ISO_DATE_TIME)
                            : ZonedDateTime.now(cstZoneId);
                        updateTime = zonedUpdateTime.withZoneSameInstant(cstZoneId).toLocalDateTime();

                        // --- 解析 items 数组 ---
                        if (orderDtoJson.has("items") && !orderDtoJson.isNull("items")) {
                            JSONArray itemsArray = orderDtoJson.getJSONArray("items");

                            for (int i = 0; i < itemsArray.length(); i++) {
                                JSONObject itemJson = itemsArray.getJSONObject(i);
                                
                                Long productId = itemJson.getLong("productId");
                                String productName = itemJson.getString("productName");
                                Integer quantity = itemJson.getInt("quantity");
                                BigDecimal unitPrice = new BigDecimal(itemJson.getString("unitPrice"));
                                BigDecimal itemTotalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
                                
                                // 1. 创建 Order 实体
                                Order orderEntity = new Order();
                                orderEntity.setOrderId(commonOrderIdString); // 假设 Order 实体的 orderId 是 String
                                orderEntity.setUserId(String.valueOf(userId)); // 假设 Order 实体的 userId 是 String
                                orderEntity.setStatus(status);
                                orderEntity.setCreateTime(createTime);
                                orderEntity.setUpdateTime(updateTime);
                                orderEntity.setProductId(String.valueOf(productId)); // 假设 Order 实体的 productId 是 String
                                orderEntity.setQuantity(quantity);
                                orderEntity.setUnitPrice(unitPrice);
                                orderEntity.setTotalAmount(itemTotalAmount);

                                // 2. 创建 Product 实体
                                Product productEntity = new Product();
                                productEntity.setProductId(productId);
                                productEntity.setProductName(productName);
                                productEntity.setUnitPrice(unitPrice);

                                // 3. 创建 OrderItem 实体
                                OrderItem orderItemEntity = new OrderItem();
                                orderItemEntity.setOrderId(commonOrderIdLong); // **修正：这里使用 Long 类型的 orderId**
                                orderItemEntity.setProductId(productId);
                                orderItemEntity.setQuantity(quantity);
                                orderItemEntity.setUnitPrice(unitPrice);
                                orderItemEntity.setTotalAmount(itemTotalAmount);

                                collector.collect(new Tuple3<>(orderEntity, productEntity, orderItemEntity));
                            }
                        } else {
                            System.out.println("Flink: 订单 " + commonOrderIdString + " 没有包含有效的订单项，不写入数据库。");
                        }
                    }
                });

        // --- 1. 将数据写入 orders 表 ---
        DataStream<Order> orderStream = processedStream.map(t -> t.f0);
        orderStream.addSink(JdbcSink.sink(
                "INSERT INTO orders (order_id, user_id, product_id, quantity, unit_price, total_amount, order_status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (statement, order) -> {
                    System.out.println("💾 Preparing to write order to MySQL: " + order);
                    statement.setString(1, order.getOrderId());
                    statement.setString(2, order.getUserId());
                    statement.setString(3, order.getProductId());
                    statement.setInt(4, order.getQuantity());
                    statement.setBigDecimal(5, order.getUnitPrice());
                    statement.setBigDecimal(6, order.getTotalAmount());
                    statement.setString(7, order.getStatus() != null ? order.getStatus().name() : null);
                    statement.setTimestamp(8, Timestamp.valueOf(order.getCreateTime()));
                    statement.setTimestamp(9, Timestamp.valueOf(order.getUpdateTime()));
                    System.out.println("💾 Finished preparing statement for OrderID: " + order.getOrderId());
                },
                JdbcExecutionOptions.builder().withBatchSize(100).withBatchIntervalMs(500).withMaxRetries(5).build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://127.0.0.1:3306/ecommerce?useSSL=false&serverTimezone=Asia/Shanghai")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("root")
                        .withPassword("093390Aa")
                        .build()
        ));
        
        // --- 2. 将数据写入 products 表 ---
        DataStream<Product> productStream = processedStream.map(t -> t.f1);
        productStream.addSink(JdbcSink.sink(
                "INSERT INTO products (product_id, product_name, unit_price) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE product_name = VALUES(product_name), unit_price = VALUES(unit_price)",
                (statement, product) -> {
                    statement.setLong(1, product.getProductId());
                    statement.setString(2, product.getProductName());
                    statement.setBigDecimal(3, product.getUnitPrice());
                    System.out.println("💾 Writing product to MySQL: " + product.getProductId());
                },
                JdbcExecutionOptions.builder().withBatchSize(100).withBatchIntervalMs(500).withMaxRetries(5).build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://127.0.0.1:3306/ecommerce?useSSL=false&serverTimezone=Asia/Shanghai")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("root")
                        .withPassword("093390Aa")
                        .build()
        ));

        // --- 3. 将数据写入 order_items 表 ---
        DataStream<OrderItem> orderItemStream = processedStream.map(t -> t.f2);
        orderItemStream.addSink(JdbcSink.sink(
                "INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_amount) VALUES (?, ?, ?, ?, ?)",
                (statement, orderItem) -> {
                    statement.setLong(1, orderItem.getOrderId()); // **修正：使用 setLong**
                    statement.setLong(2, orderItem.getProductId());
                    statement.setInt(3, orderItem.getQuantity());
                    statement.setBigDecimal(4, orderItem.getUnitPrice());
                    statement.setBigDecimal(5, orderItem.getTotalAmount());
                    System.out.println("💾 Writing order item to MySQL: " + orderItem.getOrderId());
                },
                JdbcExecutionOptions.builder().withBatchSize(100).withBatchIntervalMs(500).withMaxRetries(5).build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://127.0.0.1:3306/ecommerce?useSSL=false&serverTimezone=Asia/Shanghai")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("root")
                        .withPassword("093390Aa")
                        .build()
        ));


        env.execute("Ecommerce Order Processing Job");
    }
}
