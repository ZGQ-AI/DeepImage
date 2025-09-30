package org.tech.ai.deepimage.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.tech.ai.deepimage.constant.ResponseConstant;
import org.tech.ai.deepimage.constant.SessionStatus;
import org.tech.ai.deepimage.model.dto.request.FindSessionByTokenRequest;
import org.tech.ai.deepimage.entity.Session;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.service.SessionService;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Resource
    private SessionService sessionService;
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 1) 框架登录校验
            StpUtil.checkLogin();
            // 2) 业务会话活跃校验
            String token = StpUtil.getTokenValue();
            long userId = StpUtil.getLoginIdAsLong();
            FindSessionByTokenRequest req = new FindSessionByTokenRequest();
            req.setAccessToken(token);
            req.setUserId(userId);
            Session s = sessionService.findByAccessTokenAndUserId(req);
            if (s == null || s.getActive() == null || s.getActive() != SessionStatus.ACTIVE) {
                throw BusinessException.of(ResponseConstant.UNAUTHORIZED, ResponseConstant.NOT_LOGIN_MESSAGE);
            }
        }))
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/login","/api/auth/register","/api/auth/refresh",
                        "/api/auth/reset-password","/api/health");
    }


    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForStateless();
    }
}
