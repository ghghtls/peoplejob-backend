package com.people.job.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PeopleJob API")
                        .description("피플잡 구인구직 플랫폼 REST API 명세서\n\n" +
                                "**인증 방법**: 로그인 후 발급된 JWT 토큰을 우측 상단 🔒 Authorize 버튼에 입력하세요.\n\n" +
                                "형식: `Bearer {token}`")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("PeopleJob Team")
                                .email("ghghtls2@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:5000").description("로컬 개발 서버")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 토큰을 입력하세요 (Bearer 접두사 불필요)")));
    }
}
