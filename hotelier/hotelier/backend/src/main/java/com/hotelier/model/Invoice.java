package com.hotelier.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * OOPS: Invoice IMPLEMENTS the Billable interface and HAS-A Booking (composition).
 * It is calculated on demand from the booking, so it can never disagree with the booking.
 */
public class Invoice implements Billable {

    private final Booking booking;
    private final LocalDate issuedOn;

    public Invoice(Booking booking, LocalDate issuedOn) {
        if (booking == null) {
            throw new IllegalArgumentException("An invoice needs a booking");
        }
        this.booking = booking;
        this.issuedOn = issuedOn;
    }

    public String getInvoiceNumber() {
        return "INV-" + String.format("%05d", booking.getId() == null ? 0 : booking.getId());
    }

    @Override
    public List<InvoiceLine> getLineItems() {
        Room room = booking.getRoom();
        String description = room.getRoomType() + " room " + room.getRoomNumber() + " - nightly rate";
        return List.of(new InvoiceLine(description, booking.getNights(), booking.getNightlyRate(), booking.getTotalPrice()));
    }

    @Override
    public BigDecimal calculateSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceLine line : getLineItems()) {
            subtotal = subtotal.add(line.amount());
        }
        return subtotal;
    }

    @Override
    public BigDecimal calculateTotal() {
        return calculateSubtotal().add(calculateTax());
    }

    public Booking getBooking() { return booking; }
    public LocalDate getIssuedOn() { return issuedOn; }
}
