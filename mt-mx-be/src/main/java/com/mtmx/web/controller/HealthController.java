package com.mtmx.web.controller;

import com.mtmx.web.dto.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health check controller for Docker health monitoring
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health check operations")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Returns application health status")
    public ResponseEntity<StandardResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "service", "mt-mx-backend",
            "version", "0.0.1-SNAPSHOT"
        );
        return ResponseEntity.ok(StandardResponse.success(healthData, "Sistem sağlıklı"));
    }
} 