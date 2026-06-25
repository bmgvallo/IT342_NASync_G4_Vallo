package com.citu.nasync_backend.shared.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ClockHolder {

    private static Clock instance;

    @Autowired
    public ClockHolder(Clock clock) {
        ClockHolder.instance = clock;
    }

    public static Clock get() {
        return instance != null ? instance : Clock.systemDefaultZone();
    }
}
