package com.example.orders.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus; 

// 统一且友好的API响应格式
@Data
// @NoArgsConstructor // Lombok 会生成无参构造函数
// @AllArgsConstructor // Lombok 会生成全参构造函数
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化时忽略值为null的字段
public class ApiResponse<T> {
    private Integer code;      // 状态码，例如 200, 400, 500
    private String message; // 消息，例如 "成功", "参数错误"
    private T data;        // 实际返回的数据
    private String status;
    // private Integer code; // 使用 Integer 类型来匹配 HttpStatus.value() 的 int

    // constructor
    public ApiResponse() {
    }

    // 带有数据的成功响应3参数构造函数（可选，但如果直接调用也很方便）
    public ApiResponse(String status, String message, T data) {
        this(status, message, data, HttpStatus.OK.value()); // 默认状态码为 200 OK
    }
    // 这个构造函数接受所有四个参数，并且是最终的数据设置点
    public ApiResponse(String status, String message, T data, Integer code) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    // --- 静态工厂方法 ---

    // 无参数:你可以选择返回一个默认的成功消息，或者只表示成功不带额外信息。
    public static ApiResponse<Void> success() { // 使用 Void 表示没有具体的 data
        return new ApiResponse<>("success", "操作成功", null, HttpStatus.OK.value());
    }

    // 1. 只有数据的成功方法（根据错误信息，这个应该已经存在）
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "Operation successful", data, HttpStatus.OK.value());
    }

    // 2. 只有消息的成功方法（根据错误信息，这个应该已经存在）
    public static ApiResponse<String> success(String message) {
        return new ApiResponse<>("success", message, null, HttpStatus.OK.value());
    }

    // 3. ✨ 新增：带有消息和数据的成功方法 ✨
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data, HttpStatus.OK.value());
    }

    // 成功响应
    //public static <T> ApiResponse<T> success(T data) {
      //  return new ApiResponse<>(200, "操作成功", data);
    //}
    
    // 静态方法重写overload
    //public static ApiResponse<String> success(String message) {
      //  return new ApiResponse<>("操作成功", message, null);
    //}

    // 失败响应
    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
    	response.setCode(code);
    	response.setMessage(message);
    	response.setData(null);
    	return response;
	// return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        ApiResponse<T> response = new ApiResponse<>();
    	response.setCode(400);
    	response.setMessage(message);
    	response.setData(null);
    	return response;
  	// return new ApiResponse<>(400, message, null);
    }

    public static <T> ApiResponse<T> serverError(String message) {
        ApiResponse<T> response = new ApiResponse<>();
	response.setCode(500);
    	response.setMessage(message);
    	response.setData(null);
    	return response;
	// return new ApiResponse<>(500, message, null);
    }


    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
