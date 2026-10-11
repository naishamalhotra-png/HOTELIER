package com.hotelier.model;

import java.math.BigDecimal;

/** One itemised row on a bill (a Java record: an immutable data carrier). */
public record InvoiceLine(String description, int quantity, BigDecimal unitPrice, BigDecimal amount) {
}
