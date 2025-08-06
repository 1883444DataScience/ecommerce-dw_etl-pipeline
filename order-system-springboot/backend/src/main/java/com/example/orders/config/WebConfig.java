// src/main/java/com/example/orders/config/WebConfig.java
package com.example.orders.config; // 确保包名正确

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:5500", "http://127.0.0.1:5500")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 务必包含 OPTIONS
                    .allowedHeaders("*") // 允许所有请求头
                    .allowCredentials(true) // 允许发送 Cookie 等凭证
                    .maxAge(3600); // 预估请求的缓存时间（秒）
            }
        };
    }
}
