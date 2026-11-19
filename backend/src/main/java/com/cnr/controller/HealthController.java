package com.cnr.controller;

import com.cnr.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查：供容器探针使用（对应方案 4.3）
 */
@RestController
@Tag(name = "健康检查")
public class HealthController {

    @GetMapping("/api/health")
    @Operation(summary = "健康检查")
    public Result<Map<String, String>> health() {
        return Result.success(Map.of("status", "UP", "service", "campus-repair"));
    }
}
