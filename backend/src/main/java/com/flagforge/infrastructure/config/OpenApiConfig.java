package com.flagforge.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI flagForgeOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("FlagForge API")
						.description("Feature Flag Management Platform")
						.version("v1")
						.contact(new Contact().name("FlagForge").email("dev@flagforge.local"))
						.license(new License().name("Proprietary")))
				.components(new Components()
						.addSecuritySchemes("bearerAuth", new SecurityScheme()
								.name("bearerAuth")
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("JWT from /api/v1/auth/login or /register"))
						.addSecuritySchemes("apiKeyAuth", new SecurityScheme()
								.name("X-API-Key")
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.description("SDK key from POST /api/v1/api-keys (format ffk_<prefix>_<secret>)")));
	}
}
