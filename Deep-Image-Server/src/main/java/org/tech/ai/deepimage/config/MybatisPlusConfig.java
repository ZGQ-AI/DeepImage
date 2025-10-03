package org.tech.ai.deepimage.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 分页插件
     * 从 v3.5.9 开始，分页插件需要单独引入 mybatis-plus-jsqlparser 依赖
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件，指定数据库类型为 PostgreSQL
        // 注意：如果配置多个插件，分页插件必须放在最后
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        
        return interceptor;
    }
}

