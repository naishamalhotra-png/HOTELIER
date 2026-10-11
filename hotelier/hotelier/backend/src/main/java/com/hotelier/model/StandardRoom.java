package com.hotelier.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Standard room: nightly rate equals the base rate. */
@Entity
@DiscriminatorValue("STANDARD")
public class StandardRoom extends Room {

    protected StandardRoom() {
    }

    public StandardRoom(String roomNumber, int capacity, BigDecimal baseRate) {
        super(roomNumber, capacity, baseRate);
    }

    @Override
    public BigDecimal nightlyRate() {
        return getBaseRate().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getRoomType() {
        return "Standard";
    }
}
