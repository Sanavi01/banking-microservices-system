package com.sofka.ms_cuentas_movimientos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-cuentas-movimientos API")
                        .version("1.0")
                        .description("Microservicio de gesti\u00f3n de Cuentas, Movimientos y Reportes\n\n"
                                + "Endpoints: `/cuentas`, `/movimientos`, `/reportes`")
                        .contact(new Contact()
                                .name("Santiago Angarita Avila")
                                .email("sanavila2002@gmail.com")));
    }
}
