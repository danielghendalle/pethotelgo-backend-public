package com.api.pethotelgo.config

import org.springframework.context.annotation.Configuration
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springframework.context.annotation.Bean
import io.swagger.v3.oas.models.Components

@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Pet Hotel Management System API")
                    .version("1.0.0")
                    .description("Complete REST API for Pet Hotel Management System with JWT Authentication")
                    .contact(
                        Contact()
                            .name("Pet Hotel Team")
                            .email("support@pethotel.com")
                            .url("https://pethotel.com")
                    )
                    .license(
                        License()
                            .name("MIT")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer-jwt",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter JWT token")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("bearer-jwt"))
    }
}

