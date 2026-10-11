package com.hotelier.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Deluxe room: 15% premium on the base rate. */
@Entity
@DiscriminatorValue("DELUXE")
public class DeluxeRoom extends Room {

    private static final BigDecimal PREMIUM = new BigDecimal("1.15");

    protected DeluxeRoom() {
    }

    public DeluxeRoom(String roomNumber, int capacity, BigDecimal baseRate) {
        super(roomNumber, capacity, baseRate);
    }

    @Override
    public BigDecimal nightlyRate() {
        return getBaseRate().multiply(PREMIUM).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getRoomType() {
        return "Deluxe";
    }
}
