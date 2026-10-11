package com.hotelier.dto;

import com.hotelier.model.Booking;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(Long id, String reference, GuestResponse guest, RoomResponse room,
                              LocalDate checkInDate, LocalDate checkOutDate, int nights, int numberOfGuests,
                              BigDecimal nightlyRate, BigDecimal totalPrice, String status, String paymentStatus,
                              LocalDateTime createdAt, LocalDateTime checkedInAt, LocalDateTime checkedOutAt) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getReference(), GuestResponse.from(b.getGuest()),
                RoomResponse.from(b.getRoom()), b.getCheckInDate(), b.getCheckOutDate(), b.getNights(),
                b.getNumberOfGuests(), b.getNightlyRate(), b.getTotalPrice(), b.getStatus().name(),
                b.getPaymentStatus().name(), b.getCreatedAt(), b.getCheckedInAt(), b.getCheckedOutAt());
    }
}
