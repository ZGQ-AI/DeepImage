package org.tech.ai.deepimage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tech.ai.deepimage.model.dto.response.ApiResponse;

/**
 * 健康检查控制器，提供系统健康状态检查接口
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 健康检查接口，检查系统是否正常运行
     */
    @GetMapping
    public ApiResponse<String> health() {
        return ApiResponse.success("ok");
    }
}
