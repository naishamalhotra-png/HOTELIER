package com.hotelier.service;

import com.hotelier.exception.BookingException;
import com.hotelier.exception.InvalidDatesException;
import com.hotelier.model.Room;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

/** Pure business rules for a new booking. Contains no database code, so it is easy to test and explain. */
@Component
public class BookingValidator {

    public static final int MAX_NIGHTS = 30;

    public void validateDates(LocalDate checkIn, LocalDate checkOut, LocalDate today) throws InvalidDatesException {
        if (checkIn == null || checkOut == null) {
            throw new InvalidDatesException("Check-in and check-out dates are required");
        }
        if (checkIn.isBefore(today)) {
            throw new InvalidDatesException("Check-in date cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDatesException("Check-out must be after check-in (minimum one night)");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > MAX_NIGHTS) {
            throw new InvalidDatesException("A stay cannot be longer than " + MAX_NIGHTS + " nights");
        }
    }

    public void validateGuestCount(Room room, Integer guests) throws BookingException {
        if (guests == null || guests < 1) {
            throw new BookingException("At least one guest is required");
        }
        if (guests > room.getCapacity()) {
            throw new BookingException("Room " + room.getRoomNumber() + " fits only " + room.getCapacity()
                    + " guest(s), but " + guests + " were requested");
        }
    }
}
