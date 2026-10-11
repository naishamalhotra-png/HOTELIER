package com.hotelier.exception;

/** Thrown when a request contains invalid input (blank name, bad phone number, etc.). */
public class ValidationException extends HotelException {
    public ValidationException(String message) {
        super(message);
    }
}
