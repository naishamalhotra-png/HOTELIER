package com.hotelier.config;

import java.net.URI;

/**
 * Render gives a URL like  postgresql://user:password@host:5432/dbname
 * but Java needs            jdbc:postgresql://host:5432/dbname  plus separate user and password.
 * This small class converts one into the other.
 */
public final class JdbcUrlConverter {

    public record JdbcSettings(String url, String username, String password) {
    }

    private JdbcUrlConverter() {
    }

    public static JdbcSettings convert(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalArgumentException("DATABASE_URL is empty");
        }
        String value = databaseUrl.trim();
        if (value.startsWith("jdbc:")) {
            return new JdbcSettings(value, null, null);            // already in Java format
        }
        URI uri = URI.create(value);
        String scheme = uri.getScheme();
        if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
            throw new IllegalArgumentException("DATABASE_URL must start with postgres:// or postgresql://");
        }
        String username = null;
        String password = null;
        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            int colon = userInfo.indexOf(':');
            username = colon < 0 ? userInfo : userInfo.substring(0, colon);
            password = colon < 0 ? null : userInfo.substring(colon + 1);
        }
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String query = uri.getQuery() == null ? "" : "?" + uri.getQuery();
        String url = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + query;
        return new JdbcSettings(url, username, password);
    }
}
