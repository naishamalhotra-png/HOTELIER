package com.hotelier.model;

import com.hotelier.exception.InvalidStateException;
import com.hotelier.exception.RoomUnavailableException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * OOPS: COMPOSITION / HAS-A. A Booking HAS-A Guest and HAS-A Room (it does not extend them).
 * A Booking owns its life-cycle rules: the state-change methods below are the only way to change status.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    /** Statuses that block a room for other guests. */
    public static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private int numberOfGuests;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal nightlyRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;

    protected Booking() {
    }

    /** OOPS: CONSTRUCTOR OVERLOADING - one guest by default. */
    public Booking(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this(guest, room, checkInDate, checkOutDate, 1);
    }

    public Booking(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        if (guest == null || room == null || checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Guest, room and dates are required");
        }
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.nightlyRate = room.nightlyRate();                          // polymorphic call: depends on room type
        this.totalPrice = room.calculateCost(checkInDate, checkOutDate); // overloaded method, calculated in Java
        this.createdAt = LocalDateTime.now();
    }

    // ---------- queries ----------

    public boolean isActive() {
        return ACTIVE_STATUSES.contains(status);
    }

    public int getNights() {
        return (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public String getReference() {
        return "BK-" + String.format("%05d", id == null ? 0 : id);
    }

    /**
     * Two stays overlap when each one starts before the other ends.
     * Check-out day is free for the next guest, so [10..12) and [12..14) do NOT overlap.
     */
    public boolean overlaps(LocalDate otherCheckIn, LocalDate otherCheckOut) {
        return checkInDate.isBefore(otherCheckOut) && checkOutDate.isAfter(otherCheckIn);
    }

    // ---------- life-cycle rules (state machine) ----------

    public void checkIn(LocalDateTime now) throws InvalidStateException, RoomUnavailableException {
        if (status != BookingStatus.CONFIRMED) {
            throw new InvalidStateException("Only a CONFIRMED booking can be checked in (current status: " + status + ")");
        }
        LocalDate today = now.toLocalDate();
        if (today.isBefore(checkInDate)) {
            throw new InvalidStateException("Too early to check in. Check-in date is " + checkInDate);
        }
        if (!today.isBefore(checkOutDate)) {
            throw new InvalidStateException("This stay has already ended on " + checkOutDate);
        }
        room.markOccupied();              // may throw if the room is not free; booking stays unchanged
        status = BookingStatus.CHECKED_IN;
        checkedInAt = now;
    }

    public void checkOut(LocalDateTime now) throws InvalidStateException {
        if (status != BookingStatus.CHECKED_IN) {
            throw new InvalidStateException("Only a CHECKED_IN booking can be checked out (current status: " + status + ")");
        }
        room.markAvailable();
        status = BookingStatus.CHECKED_OUT;
        checkedOutAt = now;
    }

    public void cancel() throws InvalidStateException {
        if (status != BookingStatus.CONFIRMED) {
            throw new InvalidStateException("Only a CONFIRMED booking can be cancelled (current status: " + status + ")");
        }
        status = BookingStatus.CANCELLED;
    }

    public void markPaid() throws InvalidStateException {
        if (status == BookingStatus.CANCELLED) {
            throw new InvalidStateException("A cancelled booking cannot be paid");
        }
        if (paymentStatus == PaymentStatus.PAID) {
            throw new InvalidStateException("This booking is already paid");
        }
        paymentStatus = PaymentStatus.PAID;
    }

    // ---------- getters (no setters: state changes only through the methods above) ----------

    public Long getId() { return id; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public LocalDateTime getCheckedOutAt() { return checkedOutAt; }
}
