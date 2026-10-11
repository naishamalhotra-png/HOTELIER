package com.hotelier.service;

import com.hotelier.model.Invoice;
import com.hotelier.model.InvoiceLine;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** FILE HANDLING: writes an invoice to a .txt file. Shows try / catch / finally / throws. */
@Component
public class InvoiceExporter {

    private static final Logger log = LoggerFactory.getLogger(InvoiceExporter.class);

    private final Path exportDirectory;

    public InvoiceExporter(@Value("${hotelier.export-dir:exports}") String directory) {
        this.exportDirectory = Paths.get(directory);
    }

    /** Builds the plain-text version of an invoice. */
    public String toText(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("HOTELIER - INVOICE\n");
        sb.append("==================================================\n");
        sb.append("Invoice no : ").append(invoice.getInvoiceNumber()).append('\n');
        sb.append("Issued on  : ").append(invoice.getIssuedOn()).append('\n');
        sb.append("Booking    : ").append(invoice.getBooking().getReference()).append('\n');
        sb.append("Guest      : ").append(invoice.getBooking().getGuest().describe()).append('\n');
        sb.append("Stay       : ").append(invoice.getBooking().getCheckInDate()).append(" to ")
                .append(invoice.getBooking().getCheckOutDate()).append('\n');
        sb.append("--------------------------------------------------\n");
        for (InvoiceLine line : invoice.getLineItems()) {
            sb.append(line.description()).append('\n');
            sb.append("  ").append(line.quantity()).append(" night(s) x Rs ").append(line.unitPrice())
                    .append(" = Rs ").append(line.amount()).append('\n');
        }
        sb.append("--------------------------------------------------\n");
        sb.append("Subtotal   : Rs ").append(invoice.calculateSubtotal()).append('\n');
        sb.append("GST (12%)  : Rs ").append(invoice.calculateTax()).append('\n');
        sb.append("TOTAL      : Rs ").append(invoice.calculateTotal()).append('\n');
        sb.append("Payment    : ").append(invoice.getBooking().getPaymentStatus()).append('\n');
        return sb.toString();
    }

    /** Saves the invoice file and returns its path. 'throws IOException' tells callers they must handle it. */
    public Path export(Invoice invoice) throws IOException {
        Files.createDirectories(exportDirectory);
        Path file = exportDirectory.resolve(invoice.getInvoiceNumber() + ".txt");
        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
            writer.write(toText(invoice));
            log.info("Invoice written to {}", file.toAbsolutePath());
        } catch (IOException ex) {
            log.error("Could not write invoice file {}", file, ex);
            throw ex;                                  // 'throw' passes the problem to the caller
        } finally {
            if (writer != null) {                      // 'finally' always runs: the file is always closed
                try {
                    writer.close();
                } catch (IOException closeError) {
                    log.warn("Could not close invoice file", closeError);
                }
            }
        }
        return file;
    }
}
