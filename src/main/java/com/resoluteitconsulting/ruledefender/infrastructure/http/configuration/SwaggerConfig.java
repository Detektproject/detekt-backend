package com.resoluteitconsulting.ruledefender.infrastructure.http.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {


    @Value("${spring.security.oauth2.resourceserver.jwt.swagger-issuer-uri}")
    private String issuerUrl;

    private static final String OAUTH_SCHEME_NAME = "oAuth";

    private SecurityScheme createOAuthScheme() {

        return new SecurityScheme() //2
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .authorizationCode(
                                new OAuthFlow()
                                .authorizationUrl(issuerUrl + "/oauth2/v2.0/authorize")
                                .tokenUrl(issuerUrl + "/oauth2/v2.0/token")
                                .scopes(new Scopes().addString("openid", ""))
                        )
                );
    }


    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().
                        addList("OAuth2 Authentication"))
                .components(new Components().addSecuritySchemes
                        (OAUTH_SCHEME_NAME, createOAuthScheme()))
                .info(new Info().title("Rule Defender")
                        .description("Rule defender Backend API")
                        .version("1.0").contact(new Contact().name("Yazid AQEL")
                                .email( "www.resoluteitconsulting.com").url("contact@resoluteitconsulting.com"))
                        .license(new License().name("License of API").url("API license URL")));
    }
}
