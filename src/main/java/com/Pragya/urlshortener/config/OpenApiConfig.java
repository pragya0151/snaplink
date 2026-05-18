package com.Pragya.urlshortener.config;



import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("A production-grade URL shortener built with Spring Boot, MySQL and Redis")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Pragya")
                                .email("your-email@example.com")));
    }
}
