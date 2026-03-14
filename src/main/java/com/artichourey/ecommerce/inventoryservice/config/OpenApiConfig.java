package com.artichourey.ecommerce.inventoryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
	
	
	@Bean
	public OpenAPI inventoryServiceAPI() {
	    return new OpenAPI()
	            .info(new Info()
	                    .title("Inventory Service API")
	                    .description("Inventory management APIs")
	                    .version("1.0"));
	}

}
