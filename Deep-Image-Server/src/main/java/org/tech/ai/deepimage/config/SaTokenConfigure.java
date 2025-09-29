package org.tech.ai.deepimage.config;

import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SaTokenConfigure {
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForStateless();
    }

    // @Bean
    // public SaServletFilter saServletFilter() {
    //     return new SaServletFilter()
    //             .addInclude("/**")
    //             .addExclude("/api/auth/**")
    //             .addExclude("/api/health/**")
    //             .setAuth(obj -> StpUtil.checkLogin());
    // }
}
