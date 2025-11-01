package org.tech.ai.deepimage.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus Configuration Class
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * Configure MyBatis-Plus pagination plugin
     * Starting from v3.5.9, pagination plugin requires separate mybatis-plus-jsqlparser dependency
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // Add pagination plugin, specify database type as PostgreSQL
        // Note: If configuring multiple plugins, pagination plugin must be placed last
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        
        return interceptor;
    }
}

