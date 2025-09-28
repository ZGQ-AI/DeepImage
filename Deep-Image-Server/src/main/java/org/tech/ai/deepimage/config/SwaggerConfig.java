package org.tech.ai.deepimage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/Knife4j 配置类
 * 提供API文档生成和展示功能
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置OpenAPI信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DeepImage API 文档")
                        .description("DeepImage 图像处理项目的 RESTful API 文档")
                        .version("0.0.1")
                        .license(new License()
                                .name("Apache 2.0 License")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
