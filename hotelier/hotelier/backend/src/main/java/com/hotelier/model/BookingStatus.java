package com.hotelier.model;

/** Booking life-cycle: CONFIRMED -> CHECKED_IN -> CHECKED_OUT, or CONFIRMED -> CANCELLED. */
public enum BookingStatus { CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }
