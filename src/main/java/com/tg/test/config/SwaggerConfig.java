package com.tg.test.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("財務管理系統 API")
                        .description("申請單作業 / 傳票管理作業 / 付款作業")
                        .version("v1.0.0"))
                .tags(List.of(
                        new Tag().name("申請單作業").description("申請單的建立、提交、審核流程"),
                        new Tag().name("傳票管理作業").description("將多張申請單打包成傳票"),
                        new Tag().name("付款作業").description("對傳票執行付款流程")
                ))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地開發環境")
                ));
    }
}