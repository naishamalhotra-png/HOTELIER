package com.hotelier.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Suite: 40% premium on the base rate. */
@Entity
@DiscriminatorValue("SUITE")
public class Suite extends Room {

    private static final BigDecimal PREMIUM = new BigDecimal("1.40");

    protected Suite() {
    }

    public Suite(String roomNumber, int capacity, BigDecimal baseRate) {
        super(roomNumber, capacity, baseRate);
    }

    @Override
    public BigDecimal nightlyRate() {
        return getBaseRate().multiply(PREMIUM).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getRoomType() {
        return "Suite";
    }
}
