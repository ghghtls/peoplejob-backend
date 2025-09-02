package com.people.job.config;

import com.people.job.admin.service.ExcelService;
import com.people.job.admin.service.impl.ExcelServiceStub;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExcelFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(ExcelService.class)
    public ExcelService excelServiceFallback() {
        // 실제 구현 빈이 하나도 없으면 스텁을 주입
        return new ExcelServiceStub();
    }
}
