package com.example.livemart.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Your API")
                        .version("1.0"))
//                .addSecurityItem(new SecurityRequirement()
//                        .addList("bearer-jwt")
//                        .addList("oauth2"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization"))
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl("/oauth2/authorization/google") // or your OAuth provider
                                                .tokenUrl("/login/oauth2/code/google")
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID")
                                                        .addString("profile", "Profile")
                                                        .addString("email", "Email"))))));
    }
}
