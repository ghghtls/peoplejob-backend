package com.people.job.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 전체 경로 허용
                        .allowedOrigins("*") // 전체 origin 허용 (개발용)
                        .allowedMethods("*") // GET, POST 등 모든 메서드 허용
                        .allowedHeaders("*") // 모든 헤더 허용
                        .allowCredentials(false); // 인증정보(Cookie 등) 허용 안 함
            }
        };
    }
}
