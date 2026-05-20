package com.people.job.user.security;

import com.people.job.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        // 채용공고 GET 비로그인 허용
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/jobs/**")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/jobs")).permitAll()

                        // 공개 API (AntPathRequestMatcher 명시 — Spring Security 6.x MVC matcher 우회)
                        .requestMatchers(
                                antMatcher("/"),
                                antMatcher("/error"),
                                antMatcher("/index.html"),
                                antMatcher("/swagger-ui/**"),
                                antMatcher("/v3/api-docs/**"),
                                antMatcher("/actuator/health"),
                                antMatcher("/actuator/info"),
                                antMatcher("/api/users/login"),
                                antMatcher("/api/users/register"),
                                antMatcher("/api/users/verify"),
                                antMatcher("/api/users/check/**"),
                                antMatcher("/api/board/**"),
                                antMatcher("/api/email/**"),
                                antMatcher("/api/notice/**"),
                                antMatcher("/api/apply/check"),
                                antMatcher("/uploads/**")
                        ).permitAll()

                        // 프리플라이트 허용
                        .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()

                        // 관리자 전용
                        .requestMatchers(antMatcher("/api/admin/**")).hasRole("ADMIN")

                        // 기업회원 전용
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/api/jobs"),
                                antMatcher(HttpMethod.PUT, "/api/jobs/**"),
                                antMatcher(HttpMethod.DELETE, "/api/jobs/**")
                        ).hasRole("COMPANY")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
