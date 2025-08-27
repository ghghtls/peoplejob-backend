package com.people.job.peoplejob_backend.config;

import com.people.job.email.service.EmailService;
import com.people.job.cache.CacheService;
import com.people.job.ratelimit.RateLimitService;
import com.people.job.session.SessionService;
import com.people.job.token.TokenCacheService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
@Profile("test")
public class MockTestConfig {

    @Bean
    @Primary
    public EmailService mockEmailService() {
        return Mockito.mock(EmailService.class);
    }

    @Bean
    @Primary
    public CacheService mockCacheService() {
        return Mockito.mock(CacheService.class);
    }

    @Bean
    @Primary
    public RateLimitService mockRateLimitService() {
        return Mockito.mock(RateLimitService.class);
    }

    @Bean
    @Primary
    public SessionService mockSessionService() {
        return Mockito.mock(SessionService.class);
    }

    @Bean
    @Primary
    public TokenCacheService mockTokenCacheService() {
        return Mockito.mock(TokenCacheService.class);
    }

    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}