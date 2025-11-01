package org.tech.ai.deepimage.config;

import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.tech.ai.deepimage.constant.ResponseConstant;
import org.tech.ai.deepimage.enums.SessionStatusEnum;
import org.tech.ai.deepimage.entity.Session;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.model.dto.request.FindSessionByTokenRequest;
import org.tech.ai.deepimage.service.SessionService;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Resource
    private SessionService sessionService;
    
    // Register interceptor
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Custom interceptor to exclude OPTIONS requests
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                // OPTIONS requests directly pass through (CORS preflight requests)
                if ("OPTIONS".equals(request.getMethod())) {
                    return true;
                }
                
                // 1) Framework login check
                StpUtil.checkLogin();
                // 2) Business session active check
                String token = StpUtil.getTokenValue();
                long userId = StpUtil.getLoginIdAsLong();
                FindSessionByTokenRequest req = new FindSessionByTokenRequest();
                req.setAccessToken(token);
                req.setUserId(userId);
                Session s = sessionService.findByAccessTokenAndUserId(req);
                if (s == null || s.getActive() == null || s.getActive() != SessionStatusEnum.ACTIVE.getValue()) {
                    throw BusinessException.of(ResponseConstant.UNAUTHORIZED, ResponseConstant.NOT_LOGIN_MESSAGE);
                }
                return true;
            }
        })
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/login","/api/auth/register","/api/auth/refresh",
                        "/api/auth/reset-password","/api/health","/api/auth/google/login","/api/auth/google/callback");
    }


    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForStateless();
    }
}
