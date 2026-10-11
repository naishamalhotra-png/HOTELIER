package com.hotelier.model;

import com.hotelier.exception.RoomUnavailableException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * OOPS: ABSTRACT CLASS with ABSTRACT METHODS. A room is always a StandardRoom, DeluxeRoom or Suite.
 * Each child overrides nightlyRate(), so pricing is decided at RUN TIME by the real object type
 * (runtime polymorphism).
 * OOPS: ENCAPSULATION - the status can only change through markOccupied()/markAvailable()/markMaintenance(),
 * which enforce the rules.
 */
@Entity
@Table(name = "rooms")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "room_kind", discriminatorType = DiscriminatorType.STRING)
public abstract class Room {

    public static final int MAX_CAPACITY = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String roomNumber;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.AVAILABLE;

    protected Room() {
    }

    protected Room(String roomNumber, int capacity, BigDecimal baseRate) {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number must not be blank");
        }
        this.roomNumber = roomNumber.trim();
        this.capacity = checkCapacity(capacity);
        this.baseRate = checkRate(baseRate);
    }

    private static int checkCapacity(int capacity) {
        if (capacity < 1 || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException("Capacity must be between 1 and " + MAX_CAPACITY);
        }
        return capacity;
    }

    private static BigDecimal checkRate(BigDecimal rate) {
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException("Rate must be greater than zero");
        }
        return rate;
    }

    // ---------- abstract behaviour: every room type must define these ----------

    /** Price for one night of THIS kind of room. */
    public abstract BigDecimal nightlyRate();

    /** Readable type name, e.g. "Deluxe". */
    public abstract String getRoomType();

    // ---------- OOPS: METHOD OVERLOADING - same name, different parameters ----------

    public BigDecimal calculateCost(int nights) {
        if (nights < 1) {
            throw new IllegalArgumentException("A stay must be at least one night");
        }
        return nightlyRate().multiply(BigDecimal.valueOf(nights));
    }

    public BigDecimal calculateCost(LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return calculateCost((int) nights);
    }

    // ---------- controlled state changes (encapsulation) ----------

    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }

    public void markOccupied() throws RoomUnavailableException {
        if (status != RoomStatus.AVAILABLE) {
            throw new RoomUnavailableException("Room " + roomNumber + " cannot be occupied because it is " + status);
        }
        status = RoomStatus.OCCUPIED;
    }

    public void markAvailable() {
        status = RoomStatus.AVAILABLE;
    }

    public void markMaintenance() throws RoomUnavailableException {
        if (status == RoomStatus.OCCUPIED) {
            throw new RoomUnavailableException("Room " + roomNumber + " is occupied and cannot go to maintenance");
        }
        status = RoomStatus.MAINTENANCE;
    }

    // ---------- getters / validated setters ----------

    public Long getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public int getCapacity() { return capacity; }
    public BigDecimal getBaseRate() { return baseRate; }
    public RoomStatus getStatus() { return status; }

    public void setRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number must not be blank");
        }
        this.roomNumber = roomNumber.trim();
    }

    public void setCapacity(int capacity) { this.capacity = checkCapacity(capacity); }
    public void setBaseRate(BigDecimal baseRate) { this.baseRate = checkRate(baseRate); }
}
