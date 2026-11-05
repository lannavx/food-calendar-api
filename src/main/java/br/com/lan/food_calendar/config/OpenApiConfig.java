package br.com.lan.food_calendar.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Food Calendar API",
                version = "1.0.0",
                description = "API backend para gerenciamento de planos alimentares e controle de refeições",
                contact = @Contact(
                        name = "Lanna"
                )
        )
)
public class OpenApiConfig {
}
