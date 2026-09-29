package com.smartcash.usuarios.infrastructure.security;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartCash — Usuarios y Autenticacion API")
                        .version("1.0.0")
                        .description("Microservicio de registro de usuarios, inicio de sesion y emision de tokens JWT."));
    }
}
