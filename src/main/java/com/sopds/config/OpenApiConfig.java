package com.sopds.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sopdsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SOPDS API")
                        .description("Simple OPDS Server - API для управления электронной библиотекой")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("SOPDS")
                                .url("https://github.com/sopds"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                ));
    }
}
