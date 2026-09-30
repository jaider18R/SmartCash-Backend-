package com.smartcash.notificaciones.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartCash - Notificaciones y Alertas API")
                        .version("1.0.0")
                        .description("Microservicio para el envio de correos, alertas y consumo de API externa de Brevo")
                        .contact(new Contact().name("SmartCash Team")));
    }
}
