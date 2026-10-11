package com.hotelier.exception;

/** Thrown when a booking is moved through an illegal status transition (e.g. cancel after check-in). */
public class InvalidStateException extends BookingException {
    public InvalidStateException(String message) {
        super(message);
    }
}
