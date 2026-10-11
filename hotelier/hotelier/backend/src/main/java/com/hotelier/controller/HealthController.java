package com.hotelier.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Simple health check. Render calls this URL to know the service is alive. */
@RestController
public class HealthController {

    @GetMapping({"/api/health", "/"})
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "hotelier-backend");
    }
}
