package com.dermavet.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dermavetOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DermaVet API")
                        .description("""
                                Backend za sajt veterinarske ambulante DermaVet.
                                Omogućava zakazivanje termina, pregled kataloga usluga i lekara,
                                i administraciju termina (zaštićeno HTTP Basic prijavom).""")
                        .version("1.0.0")
                        .contact(new Contact().name("DermaVet")))
                .components(new Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("Admin nalog - podešava se kroz ADMIN_USERNAME/ADMIN_PASSWORD")));
    }
}
