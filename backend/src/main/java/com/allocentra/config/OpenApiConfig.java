package com.allocentra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI allocentraOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Allocentra API")
                .description("Professional resource allocation system with explainability, audit trails, and scenario simulation")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Allocentra")
                    .url("https://github.com/allocentra")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development server")
            ));
    }
}
