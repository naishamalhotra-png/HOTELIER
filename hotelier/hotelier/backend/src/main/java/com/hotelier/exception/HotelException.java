package com.hotelier.exception;

/** Root of all HOTELIER business errors. Unchecked so Spring rolls the transaction back automatically. */
public class HotelException extends RuntimeException {
    public HotelException(String message) {
        super(message);
    }
}
