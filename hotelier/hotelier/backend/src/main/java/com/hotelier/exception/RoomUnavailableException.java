package com.hotelier.exception;

/** Thrown when a room cannot be booked or occupied (overlap, maintenance, still occupied). */
public class RoomUnavailableException extends BookingException {
    public RoomUnavailableException(String message) {
        super(message);
    }
}
