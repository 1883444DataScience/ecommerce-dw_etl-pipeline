package com.example.orders.controller;

import com.example.orders.common.ApiResponse;
import com.example.orders.dto.UserDTO;
import com.example.orders.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/users") // 所有用户相关API的前缀
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 创建新用户（注册）
     * POST /api/users
     * @param userDTO 用户信息（例如：用户名、密码、邮箱等）
     * @return 注册成功的用户信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            logger.info("用户注册成功: {}", createdUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdUser));
        } catch (IllegalArgumentException e) {
            logger.warn("用户注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("用户注册时发生内部错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }

    /**
     * 根据ID获取用户
     * GET /api/users/{id}
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        if (userDTO == null) {
            logger.warn("未找到用户，ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "用户不存在"));
        }
        logger.info("获取用户成功，ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    /**
     * 获取所有用户
     * GET /api/users
     * @return 所有用户列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        logger.info("获取所有用户成功，共 {} 个。", users.size());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 更新用户信息
     * PUT /api/users/{id}
     * @param id 待更新的用户ID
     * @param userDTO 包含更新信息的DTO
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            userDTO.setId(id); // 确保DTO中的ID与路径ID一致
            UserDTO updatedUser = userService.updateUser(userDTO);
            logger.info("用户 {} (ID: {}) 信息已更新。", updatedUser.getUsername(), updatedUser.getId());
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (IllegalArgumentException e) {
            logger.warn("更新用户失败，ID: {}. {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        } catch (Exception e) {
            logger.error("更新用户ID: {} 时发生内部错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }

    /**
     * 删除用户
     * DELETE /api/users/{id}
     * @param id 待删除用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                logger.info("用户 (ID: {}) 已成功删除。", id);
                return ResponseEntity.ok(ApiResponse.success());
            } else {
                logger.warn("尝试删除不存在的用户 (ID: {})。", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "用户不存在或已被删除。"));
            }
        } catch (Exception e) {
            logger.error("删除用户ID: {} 时发生内部错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.serverError("服务器内部错误，请稍后再试。"));
        }
    }

    // 注意：实际应用中，你可能还需要 /api/users/login (用户登录) 等接口
}
