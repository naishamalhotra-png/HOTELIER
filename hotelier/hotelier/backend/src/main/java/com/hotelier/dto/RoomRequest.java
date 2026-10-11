package com.hotelier.dto;

import java.math.BigDecimal;

/** type is STANDARD, DELUXE or SUITE (ignored on edit). status is AVAILABLE or MAINTENANCE (optional). */
public record RoomRequest(String roomNumber, String type, Integer capacity, BigDecimal baseRate, String status) {
}
