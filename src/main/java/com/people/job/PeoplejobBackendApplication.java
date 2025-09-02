package com.people.job;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = { "com.people.job.**.mapper" })
public class PeoplejobBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PeoplejobBackendApplication.class, args);
    }
}