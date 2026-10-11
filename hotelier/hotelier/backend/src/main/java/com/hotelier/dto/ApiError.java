package com.hotelier.dto;

import java.time.LocalDateTime;

/** Standard error body returned for every failed request. */
public record ApiError(int status, String error, String message, LocalDateTime timestamp) {
}
