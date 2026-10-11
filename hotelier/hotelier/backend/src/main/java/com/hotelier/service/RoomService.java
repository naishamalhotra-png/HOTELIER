package com.hotelier.service;

import com.hotelier.dto.RoomRequest;
import com.hotelier.exception.ResourceNotFoundException;
import com.hotelier.exception.RoomUnavailableException;
import com.hotelier.exception.ValidationException;
import com.hotelier.model.Booking;
import com.hotelier.model.DeluxeRoom;
import com.hotelier.model.Room;
import com.hotelier.model.RoomStatus;
import com.hotelier.model.StandardRoom;
import com.hotelier.model.Suite;
import com.hotelier.repository.BookingRepository;
import com.hotelier.repository.RoomRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

    private static final BigDecimal MAX_RATE = new BigDecimal("1000000");

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final BookingValidator validator;
    private final Clock clock;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository,
                       BookingValidator validator, Clock clock) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Room> search(String query, String type, String status, Integer minCapacity) {
        String q = query == null ? "" : query.trim().toLowerCase();
        String wantedType = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        String wantedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        return roomRepository.findAll().stream()
                .filter(r -> q.isEmpty() || r.getRoomNumber().toLowerCase().contains(q))
                .filter(r -> wantedType.isEmpty() || r.getRoomType().toUpperCase(Locale.ROOT).equals(wantedType))
                .filter(r -> wantedStatus.isEmpty() || r.getStatus().name().equals(wantedStatus))
                .filter(r -> minCapacity == null || r.getCapacity() >= minCapacity)
                .sorted((a, b) -> a.getRoomNumber().compareToIgnoreCase(b.getRoomNumber()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Room get(Long id) throws ResourceNotFoundException {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + id + " was not found"));
    }

    /** Rooms that are not in maintenance, fit the guests, and have no active booking overlapping the dates. */
    @Transactional(readOnly = true)
    public List<Room> findAvailableForDates(LocalDate checkIn, LocalDate checkOut, Integer guests) {
        validator.validateDates(checkIn, checkOut, LocalDate.now(clock));
        Set<Long> busyRoomIds = new HashSet<>();
        for (Booking b : bookingRepository.findByStatusIn(Booking.ACTIVE_STATUSES)) {
            if (b.overlaps(checkIn, checkOut)) {
                busyRoomIds.add(b.getRoom().getId());
            }
        }
        return roomRepository.findAll().stream()
                .filter(r -> r.getStatus() != RoomStatus.MAINTENANCE)
                .filter(r -> !busyRoomIds.contains(r.getId()))
                .filter(r -> guests == null || r.getCapacity() >= guests)
                .sorted((a, b) -> a.getRoomNumber().compareToIgnoreCase(b.getRoomNumber()))
                .toList();
    }

    @Transactional
    public Room create(RoomRequest request) throws ValidationException {
        validateCommon(request);
        String number = request.roomNumber().trim();
        if (roomRepository.existsByRoomNumber(number)) {
            throw new ValidationException("Room number " + number + " already exists");
        }
        // Runtime polymorphism: one parent-type variable, three possible child objects.
        Room room;
        String type = request.type() == null ? "" : request.type().trim().toUpperCase(Locale.ROOT);
        switch (type) {
            case "STANDARD" -> room = new StandardRoom(number, request.capacity(), request.baseRate());
            case "DELUXE" -> room = new DeluxeRoom(number, request.capacity(), request.baseRate());
            case "SUITE" -> room = new Suite(number, request.capacity(), request.baseRate());
            default -> throw new ValidationException("Room type must be STANDARD, DELUXE or SUITE");
        }
        return roomRepository.save(room);
    }

    @Transactional
    public Room update(Long id, RoomRequest request) throws ValidationException, ResourceNotFoundException {
        validateCommon(request);
        Room room = get(id);
        String number = request.roomNumber().trim();
        if (!number.equalsIgnoreCase(room.getRoomNumber()) && roomRepository.existsByRoomNumber(number)) {
            throw new ValidationException("Room number " + number + " already exists");
        }
        applyStatusChange(room, request.status());
        room.setRoomNumber(number);
        room.setCapacity(request.capacity());
        room.setBaseRate(request.baseRate());
        return room;
    }

    /** Only AVAILABLE and MAINTENANCE can be set by hand. OCCUPIED comes from check-in/check-out. */
    private void applyStatusChange(Room room, String requested) throws ValidationException {
        if (requested == null || requested.isBlank()) {
            return;
        }
        RoomStatus wanted;
        try {
            wanted = RoomStatus.valueOf(requested.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Status must be AVAILABLE or MAINTENANCE");
        }
        if (wanted == room.getStatus()) {
            return;
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new ValidationException("An occupied room can only be released by checking the guest out");
        }
        switch (wanted) {
            case MAINTENANCE -> {
                try {
                    room.markMaintenance();
                } catch (RoomUnavailableException ex) {
                    throw new ValidationException(ex.getMessage());
                }
            }
            case AVAILABLE -> room.markAvailable();
            default -> throw new ValidationException("Status OCCUPIED is set automatically at check-in");
        }
    }

    private void validateCommon(RoomRequest r) throws ValidationException {
        if (r == null || r.roomNumber() == null || r.roomNumber().isBlank()) {
            throw new ValidationException("Room number is required");
        }
        if (r.roomNumber().trim().length() > 10) {
            throw new ValidationException("Room number is too long (maximum 10 characters)");
        }
        if (r.capacity() == null || r.capacity() < 1 || r.capacity() > Room.MAX_CAPACITY) {
            throw new ValidationException("Capacity must be between 1 and " + Room.MAX_CAPACITY);
        }
        if (r.baseRate() == null || r.baseRate().signum() <= 0 || r.baseRate().compareTo(MAX_RATE) > 0) {
            throw new ValidationException("Price per night must be greater than 0 and at most 1,000,000");
        }
    }

    @Transactional(readOnly = true)
    public long countByStatus(RoomStatus status) {
        return roomRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long count() {
        return roomRepository.count();
    }
}
