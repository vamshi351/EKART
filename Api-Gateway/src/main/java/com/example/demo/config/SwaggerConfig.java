//package com.example.demo.config;
//
//
//
//import io.swagger.v3.oas.models.Components;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.security.SecurityRequirement;
//import io.swagger.v3.oas.models.security.SecurityScheme;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SwaggerConfig {
//
//    @Bean
//    public OpenAPI customOpenAPI() {
//        final String securitySchemeName = "bearerAuth";
//
//        return new OpenAPI()
//                .info(new Info().title("Your API").version("1.0"))
//                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
//                .components(new Components()
//                    .addSecuritySchemes(securitySchemeName,
//                        new SecurityScheme()
//                            .name(securitySchemeName)
//                            .type(SecurityScheme.Type.HTTP)
//                            .scheme("bearer")
//                            .bearerFormat("JWT")));
//    }
//}

package com.example.demo.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi userServiceApi() {
        return GroupedOpenApi.builder()
                .group("user-service")
                .pathsToMatch("/api/users/**")
                .addOpenApiMethodFilter(method -> true)
                .build();
    }

    @Bean
    public GroupedOpenApi productServiceApi() {
        return GroupedOpenApi.builder()
                .group("product-service")
                .pathsToMatch("/api/products/**")
                .addOpenApiMethodFilter(method -> true)
                .build();
    }
}
