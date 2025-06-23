package com.mtmx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",     // React dev server
                    "http://127.0.0.1:3000",    // Alternative localhost
                    "http://localhost:8080",     // Alternative frontend port
                    "http://127.0.0.1:8080"     // Alternative localhost
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight response for 1 hour
        
        // Swagger UI için ayrı mapping
        registry.addMapping("/swagger-ui/**")
                .allowedOrigins("*")
                .allowedMethods("GET")
                .allowedHeaders("*");
                
        // API docs için ayrı mapping
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins("*")
                .allowedMethods("GET")
                .allowedHeaders("*");
    }
} 