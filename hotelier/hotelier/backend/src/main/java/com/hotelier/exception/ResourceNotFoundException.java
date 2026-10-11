package com.hotelier.exception;

/** Thrown when a guest, room or booking id does not exist. */
public class ResourceNotFoundException extends HotelException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
