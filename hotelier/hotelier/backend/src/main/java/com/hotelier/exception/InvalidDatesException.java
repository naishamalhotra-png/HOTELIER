package com.hotelier.exception;

/** Thrown for impossible stay dates (past date, check-out not after check-in, stay too long). */
public class InvalidDatesException extends BookingException {
    public InvalidDatesException(String message) {
        super(message);
    }
}
