package com.t1f5.skib.global.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {

        Info info = new Info()
                .version("v1.0.0")
                .title("SKIB API")
                .description("SKIB API 명세입니다.");

        return new OpenAPI()
                .info(info);
    }
}
