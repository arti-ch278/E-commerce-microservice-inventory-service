package com.artichourey.ecommerce.inventoryservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryServiceAPI() {

        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization") 
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("Inventory Management APIs for E-commerce Platform")
                        .version("1.0"))

                
                .servers(List.of(new Server().url("/")))

                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))

                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }
}
