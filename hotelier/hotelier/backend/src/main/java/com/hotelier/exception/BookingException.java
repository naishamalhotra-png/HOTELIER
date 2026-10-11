package com.hotelier.exception;

/** Custom exception for any invalid booking operation (inheritance: BookingException IS-A HotelException). */
public class BookingException extends HotelException {
    public BookingException(String message) {
        super(message);
    }
}
