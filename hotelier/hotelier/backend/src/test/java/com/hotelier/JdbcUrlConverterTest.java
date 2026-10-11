package com.hotelier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hotelier.config.JdbcUrlConverter;
import org.junit.jupiter.api.Test;

class JdbcUrlConverterTest {

    @Test
    void convertsRenderStyleUrl() {
        var s = JdbcUrlConverter.convert("postgresql://hotel_user:s3cret@dpg-abc123-a:5432/hotelier_db");
        assertEquals("jdbc:postgresql://dpg-abc123-a:5432/hotelier_db", s.url());
        assertEquals("hotel_user", s.username());
        assertEquals("s3cret", s.password());
    }

    @Test
    void usesDefaultPortWhenMissing_andAcceptsPostgresScheme() {
        var s = JdbcUrlConverter.convert("postgres://u:p@host.example.com/db");
        assertEquals("jdbc:postgresql://host.example.com:5432/db", s.url());
    }

    @Test
    void keepsPasswordWithSpecialCharacters_andQueryString() {
        var s = JdbcUrlConverter.convert("postgresql://u:p%40ss@host:5432/db?sslmode=require");
        assertEquals("p@ss", s.password());
        assertEquals("jdbc:postgresql://host:5432/db?sslmode=require", s.url());
    }

    @Test
    void jdbcUrlPassesThrough_andBadSchemeIsRejected() {
        assertEquals("jdbc:postgresql://h:5432/d", JdbcUrlConverter.convert("jdbc:postgresql://h:5432/d").url());
        assertThrows(IllegalArgumentException.class, () -> JdbcUrlConverter.convert("mysql://u:p@h/d"));
        assertThrows(IllegalArgumentException.class, () -> JdbcUrlConverter.convert(""));
    }
}
