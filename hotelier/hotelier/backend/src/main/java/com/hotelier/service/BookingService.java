package com.hotelier.service;

import com.hotelier.dto.BookingRequest;
import com.hotelier.exception.BookingException;
import com.hotelier.exception.ResourceNotFoundException;
import com.hotelier.exception.RoomUnavailableException;
import com.hotelier.exception.ValidationException;
import com.hotelier.model.Booking;
import com.hotelier.model.BookingStatus;
import com.hotelier.model.Guest;
import com.hotelier.model.Room;
import com.hotelier.model.RoomStatus;
import com.hotelier.repository.BookingRepository;
import com.hotelier.repository.GuestRepository;
import com.hotelier.repository.RoomRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * All booking business rules live here (not in the controller).
 * Every write method is @Transactional: if any exception is thrown, the database changes are rolled back.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final BookingValidator validator;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          GuestRepository guestRepository, BookingValidator validator, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Booking> search(String status, String query) {
        String wanted = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        String q = query == null ? "" : query.trim().toLowerCase();
        return bookingRepository.findAllNewestFirst().stream()
                .filter(b -> wanted.isEmpty() || b.getStatus().name().equals(wanted))
                .filter(b -> q.isEmpty()
                        || b.getGuest().getName().toLowerCase().contains(q)
                        || b.getRoom().getRoomNumber().toLowerCase().contains(q)
                        || b.getReference().toLowerCase().contains(q))
                .toList();
    }

    @Transactional(readOnly = true)
    public Booking get(Long id) throws ResourceNotFoundException {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking " + id + " was not found"));
    }

    @Transactional
    public Booking createBooking(BookingRequest request) throws BookingException {
        if (request == null || request.guestId() == null || request.roomId() == null) {
            throw new ValidationException("Please select a guest and a room");
        }
        LocalDate today = LocalDate.now(clock);
        validator.validateDates(request.checkInDate(), request.checkOutDate(), today);

        Guest guest = guestRepository.findById(request.guestId())
                .orElseThrow(() -> new ResourceNotFoundException("Guest " + request.guestId() + " was not found"));
        // Lock the room row: concurrent bookings for the same room wait here, so overlaps cannot slip through.
        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room " + request.roomId() + " was not found"));

        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new RoomUnavailableException("Room " + room.getRoomNumber() + " is under maintenance");
        }
        validator.validateGuestCount(room, request.numberOfGuests());

        for (Booking existing : bookingRepository.findByRoomAndStatuses(room.getId(), Booking.ACTIVE_STATUSES)) {
            if (existing.overlaps(request.checkInDate(), request.checkOutDate())) {
                throw new RoomUnavailableException("Room " + room.getRoomNumber() + " is already booked from "
                        + existing.getCheckInDate() + " to " + existing.getCheckOutDate());
            }
        }

        Booking booking = new Booking(guest, room, request.checkInDate(), request.checkOutDate(),
                request.numberOfGuests());
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancel(Long id) throws BookingException {
        Booking booking = get(id);
        booking.cancel();
        return booking;
    }

    @Transactional
    public Booking checkIn(Long id) throws BookingException {
        Booking booking = get(id);
        roomRepository.findByIdForUpdate(booking.getRoom().getId());   // lock the room while its status changes
        booking.checkIn(LocalDateTime.now(clock));
        return booking;
    }

    @Transactional
    public Booking checkOut(Long id) throws BookingException {
        Booking booking = get(id);
        roomRepository.findByIdForUpdate(booking.getRoom().getId());
        booking.checkOut(LocalDateTime.now(clock));
        return booking;
    }

    @Transactional
    public Booking markPaid(Long id) throws BookingException {
        Booking booking = get(id);
        booking.markPaid();
        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> findActive() {
        return bookingRepository.findByStatusIn(Booking.ACTIVE_STATUSES);
    }

    @Transactional(readOnly = true)
    public long countByStatus(BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAllNewestFirst();
    }
}
