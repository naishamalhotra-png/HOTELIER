package com.hotelier.controller;

import com.hotelier.dto.BookingRequest;
import com.hotelier.dto.BookingResponse;
import com.hotelier.dto.InvoiceResponse;
import com.hotelier.service.BillingService;
import com.hotelier.service.BookingService;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BillingService billingService;

    public BookingController(BookingService bookingService, BillingService billingService) {
        this.bookingService = bookingService;
        this.billingService = billingService;
    }

    @GetMapping
    public List<BookingResponse> list(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) String q) {
        return bookingService.search(status, q).stream().map(BookingResponse::from).toList();
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) {
        return BookingResponse.from(bookingService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@RequestBody BookingRequest request) {
        return BookingResponse.from(bookingService.createBooking(request));
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return BookingResponse.from(bookingService.cancel(id));
    }

    @PostMapping("/{id}/check-in")
    public BookingResponse checkIn(@PathVariable Long id) {
        return BookingResponse.from(bookingService.checkIn(id));
    }

    @PostMapping("/{id}/check-out")
    public BookingResponse checkOut(@PathVariable Long id) {
        return BookingResponse.from(bookingService.checkOut(id));
    }

    @PostMapping("/{id}/pay")
    public BookingResponse pay(@PathVariable Long id) {
        return BookingResponse.from(bookingService.markPaid(id));
    }

    @GetMapping("/{id}/invoice")
    public InvoiceResponse invoice(@PathVariable Long id) {
        return InvoiceResponse.from(billingService.generateInvoice(id));
    }

    /** Saves a .txt copy on the server (file handling) and returns it as a download. */
    @GetMapping("/{id}/invoice/export")
    public ResponseEntity<String> exportInvoice(@PathVariable Long id) {
        String text = billingService.exportInvoice(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + id + ".txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(text);
    }
}
