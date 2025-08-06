// src/main/java/com/example/orders/repository/OrderRepository.java
package com.example.orders.repository;

import com.example.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> { // 假设主键是Long
    // 你可以在这里定义自定义的查询方法
}
