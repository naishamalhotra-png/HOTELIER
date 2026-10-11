package com.hotelier.service;

import com.hotelier.exception.HotelException;
import com.hotelier.exception.InvalidStateException;
import com.hotelier.model.Booking;
import com.hotelier.model.BookingStatus;
import com.hotelier.model.Invoice;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final BookingService bookingService;
    private final InvoiceExporter exporter;
    private final Clock clock;

    public BillingService(BookingService bookingService, InvoiceExporter exporter, Clock clock) {
        this.bookingService = bookingService;
        this.exporter = exporter;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Invoice generateInvoice(Long bookingId) throws InvalidStateException {
        Booking booking = bookingService.get(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidStateException("A cancelled booking has no invoice");
        }
        return new Invoice(booking, LocalDate.now(clock));
    }

    /** Creates the invoice, saves a .txt copy on the server, and returns the text for download. */
    @Transactional(readOnly = true)
    public String exportInvoice(Long bookingId) throws HotelException {
        Invoice invoice = generateInvoice(bookingId);
        try {
            exporter.export(invoice);
        } catch (IOException ex) {
            // The user still gets the invoice text; only the server copy failed.
            log.warn("Could not save invoice file on the server: {}", ex.getMessage());
        }
        return exporter.toText(invoice);
    }
}
