package com.hotelier.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Used only when SPRING_PROFILES_ACTIVE=postgres (production). Locally the app uses H2. */
@Configuration
@Profile("postgres")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl,
                                 @Value("${DB_USERNAME:}") String usernameOverride,
                                 @Value("${DB_PASSWORD:}") String passwordOverride) {
        JdbcUrlConverter.JdbcSettings settings = JdbcUrlConverter.convert(databaseUrl);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(settings.url());
        config.setUsername(usernameOverride.isEmpty() ? settings.username() : usernameOverride);
        config.setPassword(passwordOverride.isEmpty() ? settings.password() : passwordOverride);
        config.setMaximumPoolSize(5);
        return new HikariDataSource(config);
    }
}
