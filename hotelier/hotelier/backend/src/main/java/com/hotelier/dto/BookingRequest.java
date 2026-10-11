package com.hotelier.dto;

import java.time.LocalDate;

public record BookingRequest(Long guestId, Long roomId, LocalDate checkInDate, LocalDate checkOutDate,
                             Integer numberOfGuests) {
}
