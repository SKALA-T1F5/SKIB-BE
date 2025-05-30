package com.t1f5.skib.global.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

     @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                                .components(new Components()
                                                .addSecuritySchemes("JWT", createAPIKeyScheme()))
                                .info(new Info()
                                    .version("v1.0.0")
                                    .title("SKIB API")
                                    .description("SKIB API 명세입니다."));

        }

    private SecurityScheme createAPIKeyScheme() {
                return new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .bearerFormat("JWT")
                                .scheme("bearer");
        }
}
