package com.example.orders.service;

import com.example.orders.dto.UserDTO;
import com.example.orders.entity.User;
import com.example.orders.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 导入事务注解
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// 为了密码安全，这里仅做示例，实际应用中密码需要加密（如BCryptPasswordEncoder）
// 并且登录等逻辑会更复杂

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    /**
     * 创建新用户
     * @param userDTO 包含用户信息的DTO
     * @return 创建成功的用户DTO
     */
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (userMapper.selectUserByUsername(userDTO.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在: " + userDTO.getUsername());
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        // 密码哈希值通常在注册时由前端传递过来或后端生成
        // 这里只是示例，实际应该对密码进行加密，例如：
        // user.setPasswordHash(passwordEncoder.encode(rawPassword));
        // user.setPasswordHash(userDTO.getPasswordHash()); // 假设DTO中包含passwordHash
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setStatus("ACTIVE"); // 默认激活状态
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insertUser(user);
        logger.info("用户 {} (ID: {}) 已创建。", user.getUsername(), user.getId());
        return convertToUserDTO(user);
    }

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户DTO
     */
    public UserDTO getUserById(Long id) {
        User user = userMapper.selectUserById(id);
        return user != null ? convertToUserDTO(user) : null;
    }

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户DTO
     */
    public UserDTO getUserByUsername(String username) {
        User user = userMapper.selectUserByUsername(username);
        return user != null ? convertToUserDTO(user) : null;
    }

    /**
     * 获取所有用户
     * @return 用户DTO列表
     */
    public List<UserDTO> getAllUsers() {
        return userMapper.selectAllUsers().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * 更新用户信息
     * @param userDTO 待更新的用户信息
     * @return 更新后的用户DTO
     */
    @Transactional
    public UserDTO updateUser(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new IllegalArgumentException("更新用户时ID不能为空。");
        }
        User existingUser = userMapper.selectUserById(userDTO.getId());
        if (existingUser == null) {
            throw new IllegalArgumentException("用户不存在，ID: " + userDTO.getId());
        }

        // 仅更新允许修改的字段
        if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getStatus() != null) {
            existingUser.setStatus(userDTO.getStatus());
        }
        existingUser.setUpdatedAt(LocalDateTime.now());

        userMapper.updateUser(existingUser);
        logger.info("用户 {} (ID: {}) 已更新。", existingUser.getUsername(), existingUser.getId());
        return convertToUserDTO(existingUser);
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteUser(Long id) {
        int rowsAffected = userMapper.deleteUserById(id);
        if (rowsAffected > 0) {
            logger.info("用户 (ID: {}) 已删除。", id);
            return true;
        }
        logger.warn("尝试删除不存在的用户 (ID: {})。", id);
        return false;
    }

    // 辅助方法：Entity 转换为 DTO
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus());
        return dto;
    }

    // 辅助方法：DTO 转换为 Entity (这里主要用于插入，更新时通常加载现有Entity)
    private User convertToUserEntity(UserDTO dto) {
        User entity = new User();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setStatus(dto.getStatus());
        return entity;
    }
}
