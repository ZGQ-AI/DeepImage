package org.tech.ai.deepimage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tech.ai.deepimage.response.ApiResponse;

/**
 * 健康检查控制器
 * 提供系统健康状态检查接口
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "健康检查", description = "系统健康状态检查接口")
public class HealthController {

    /**
     * 健康检查接口
     */
    @GetMapping
    @Operation(summary = "健康检查", description = "检查系统是否正常运行")
    public ApiResponse<String> health() {
        return ApiResponse.success("ok");
    }
}
