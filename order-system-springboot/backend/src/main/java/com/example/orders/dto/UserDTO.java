package com.example.orders.dto;

// 用户DTO，根据前端交互需求定制
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String status;
    // 注册或登录时可能需要 password，但通常不在DTO中直接传输hashed密码

    // 构造函数
    public UserDTO() {}

    public UserDTO(Long id, String username, String email, String phoneNumber, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "UserDTO{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", status='" + status + '\'' +
               '}';
    }
}
