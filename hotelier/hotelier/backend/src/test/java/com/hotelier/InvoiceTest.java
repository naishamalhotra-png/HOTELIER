package com.hotelier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hotelier.model.Billable;
import com.hotelier.model.Booking;
import com.hotelier.model.DeluxeRoom;
import com.hotelier.model.Guest;
import com.hotelier.model.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    @Test
    void invoice_isBillable_andCalculatesSubtotalTaxAndTotal() {
        Booking booking = new Booking(new Guest("Aarav Mehta", "9000000001"),
                new DeluxeRoom("201", 3, new BigDecimal("1000")), LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 3), 2);

        Billable bill = new Invoice(booking, LocalDate.of(2030, 1, 3));    // interface reference

        assertEquals(1, bill.getLineItems().size());
        assertEquals(2, bill.getLineItems().get(0).quantity());
        assertEquals(new BigDecimal("1150.00"), bill.getLineItems().get(0).unitPrice());
        assertEquals(new BigDecimal("2300.00"), bill.calculateSubtotal());
        assertEquals(new BigDecimal("276.00"), bill.calculateTax());        // 12% default method from the interface
        assertEquals(new BigDecimal("2576.00"), bill.calculateTotal());
    }

    @Test
    void invoiceNumber_usesBookingId_andLineMentionsRoom() {
        Booking booking = new Booking(new Guest("Aarav Mehta", "9000000001"),
                new DeluxeRoom("201", 3, new BigDecimal("1000")), LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2));
        Invoice invoice = new Invoice(booking, LocalDate.of(2030, 1, 2));
        assertEquals("INV-00000", invoice.getInvoiceNumber());              // id is null until saved
        assertTrue(invoice.getLineItems().get(0).description().contains("Deluxe room 201"));
    }
}
