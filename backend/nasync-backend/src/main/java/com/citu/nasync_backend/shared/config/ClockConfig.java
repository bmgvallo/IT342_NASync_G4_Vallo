package com.citu.nasync_backend.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.fixed(
            LocalDate.of(2026, 5, 23)
                     .atTime(12, 01)
                     .toInstant(ZoneOffset.of("+08:00")),
            ZoneId.of("Asia/Manila")
        );
    }
}
