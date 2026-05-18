package com.people.job;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = { "com.people.job.**.mapper" })
public class PeoplejobBackendApplication {
    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(PeoplejobBackendApplication.class, args);
    }

   
    private static void loadEnvFile() {
        File envFile = new File(".env");
        if (!envFile.exists()) return;
        try (BufferedReader reader = Files.newBufferedReader(envFile.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    System.setProperty(key, value);
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: .env load failed: " + e.getMessage());
        }
        // Map critical env-var keys to exact Spring property keys.
        // This ensures the .env value wins even if a conflicting OS env var exists.
        Map.of(
            "SPRING_DATASOURCE_URL",      "spring.datasource.url",
            "SPRING_DATASOURCE_USERNAME", "spring.datasource.username",
            "SPRING_DATASOURCE_PASSWORD", "spring.datasource.password"
        ).forEach((envKey, springKey) -> {
            String value = System.getProperty(envKey);
            if (value != null) System.setProperty(springKey, value);
        });
    }
}
