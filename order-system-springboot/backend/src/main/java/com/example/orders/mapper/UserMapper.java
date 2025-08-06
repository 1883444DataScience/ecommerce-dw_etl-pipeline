package com.example.orders.mapper;

import com.example.orders.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {
    int insertUser(User user);
    User selectUserById(Long id);
    User selectUserByUsername(String username); // 根据用户名查询用户，用于登录
    List<User> selectAllUsers();
    int updateUser(User user);
    int deleteUserById(Long id);
    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
