package com.people.job.config;

// CORS는 WebSecurityConfig.corsConfigurationSource() 와 WebConfig.addCorsMappings() 에서 처리
// 이 클래스의 corsConfigurer() 는 allowedOrigins("*") + allowCredentials(false) 조합으로
// WebConfig의 allowedOriginPatterns("*") + allowCredentials(true) 와 충돌하므로 제거
public class CorsConfig {
}
