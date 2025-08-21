package com.people.job.config;

import com.people.job.email.service.EmailService;
import com.people.job.cache.CacheService;
import com.people.job.ratelimit.RateLimitService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

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
}