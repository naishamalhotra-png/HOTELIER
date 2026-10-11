package com.hotelier.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * OOPS: INTERFACE - a contract for anything that can produce a bill.
 * Invoice implements it. The default method shows that an interface can share ready-made behaviour.
 */
public interface Billable {

    /** GST rate applied on the room charges (constant: public static final by default). */
    BigDecimal GST_RATE = new BigDecimal("0.12");

    List<InvoiceLine> getLineItems();

    BigDecimal calculateSubtotal();

    BigDecimal calculateTotal();

    default BigDecimal calculateTax() {
        return calculateSubtotal().multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
