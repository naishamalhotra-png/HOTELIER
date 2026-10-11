package com.hotelier.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** One shared Clock so "today" follows the hotel's time zone (cloud servers run in UTC). */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${hotelier.timezone:Asia/Kolkata}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
