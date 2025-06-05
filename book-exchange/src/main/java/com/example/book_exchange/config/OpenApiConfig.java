package com.example.book_exchange.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Exchange Platform API")
                        .version("1.0.0")
                        .description("API for managing book exchanges between users")
                        .contact(new Contact()
                                .name("Support Team")
                                .email("The.scorpion2006@yandex.ru")));
    }

    @Bean
    public GroupedOpenApi customApi() {
        return GroupedOpenApi.builder()
                .group("book-exchange-api")
                .packagesToScan("com.example.book_exchange.controller")
                .build();
    }
}