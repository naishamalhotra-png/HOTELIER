package com.hotelier.dto;

import com.hotelier.model.Billable;
import com.hotelier.model.Booking;
import com.hotelier.model.Invoice;
import com.hotelier.model.InvoiceLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(String invoiceNumber, LocalDate issuedOn, String bookingReference,
                              String guestName, String guestCode, String guestPhone,
                              String roomNumber, String roomType,
                              LocalDate checkInDate, LocalDate checkOutDate, int nights,
                              List<InvoiceLine> lines, BigDecimal subtotal, BigDecimal gstPercent,
                              BigDecimal tax, BigDecimal total, String bookingStatus, String paymentStatus) {

    public static InvoiceResponse from(Invoice invoice) {
        Booking b = invoice.getBooking();
        return new InvoiceResponse(invoice.getInvoiceNumber(), invoice.getIssuedOn(), b.getReference(),
                b.getGuest().getName(), b.getGuest().getGuestCode(), b.getGuest().getPhone(),
                b.getRoom().getRoomNumber(), b.getRoom().getRoomType(),
                b.getCheckInDate(), b.getCheckOutDate(), b.getNights(),
                invoice.getLineItems(), invoice.calculateSubtotal(),
                Billable.GST_RATE.movePointRight(2), invoice.calculateTax(), invoice.calculateTotal(),
                b.getStatus().name(), b.getPaymentStatus().name());
    }
}
