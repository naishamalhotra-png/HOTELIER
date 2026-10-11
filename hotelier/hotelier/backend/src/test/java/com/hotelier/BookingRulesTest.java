package com.hotelier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hotelier.exception.BookingException;
import com.hotelier.exception.InvalidDatesException;
import com.hotelier.exception.InvalidStateException;
import com.hotelier.exception.RoomUnavailableException;
import com.hotelier.model.Booking;
import com.hotelier.model.BookingStatus;
import com.hotelier.model.Guest;
import com.hotelier.model.PaymentStatus;
import com.hotelier.model.Room;
import com.hotelier.model.RoomStatus;
import com.hotelier.model.Suite;
import com.hotelier.model.StandardRoom;
import com.hotelier.service.BookingValidator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingRulesTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 6, 10);
    private final BookingValidator validator = new BookingValidator();
    private Guest guest;
    private Room room;

    @BeforeEach
    void setUp() {
        guest = new Guest("Aarav Mehta", "9000000001");
        room = new StandardRoom("101", 2, new BigDecimal("2500"));
    }

    // ---------- date validation ----------

    @Test
    void validDates_areAccepted() {
        validator.validateDates(TODAY, TODAY.plusDays(2), TODAY);
    }

    @Test
    void pastCheckIn_isRejected() {
        assertThrows(InvalidDatesException.class, () -> validator.validateDates(TODAY.minusDays(1), TODAY.plusDays(1), TODAY));
    }

    @Test
    void checkOutNotAfterCheckIn_isRejected() {
        assertThrows(InvalidDatesException.class, () -> validator.validateDates(TODAY, TODAY, TODAY));
        assertThrows(InvalidDatesException.class, () -> validator.validateDates(TODAY.plusDays(3), TODAY.plusDays(1), TODAY));
    }

    @Test
    void tooLongStay_isRejected() {
        assertThrows(InvalidDatesException.class, () -> validator.validateDates(TODAY, TODAY.plusDays(31), TODAY));
        validator.validateDates(TODAY, TODAY.plusDays(30), TODAY);
    }

    @Test
    void missingDates_areRejected() {
        assertThrows(InvalidDatesException.class, () -> validator.validateDates(null, TODAY, TODAY));
    }

    // ---------- capacity ----------

    @Test
    void capacity_isEnforced() {
        validator.validateGuestCount(room, 2);
        assertThrows(BookingException.class, () -> validator.validateGuestCount(room, 3));
        assertThrows(BookingException.class, () -> validator.validateGuestCount(room, 0));
        assertThrows(BookingException.class, () -> validator.validateGuestCount(room, null));
    }

    // ---------- pricing ----------

    @Test
    void totalPrice_isCalculatedInJava() {
        Booking b = new Booking(guest, room, TODAY, TODAY.plusDays(3));
        assertEquals(3, b.getNights());
        assertEquals(new BigDecimal("2500.00"), b.getNightlyRate());
        assertEquals(new BigDecimal("7500.00"), b.getTotalPrice());
    }

    @Test
    void suiteBooking_usesSuiteRate() {
        Room suite = new Suite("301", 4, new BigDecimal("7500"));
        Booking b = new Booking(guest, suite, TODAY, TODAY.plusDays(2), 3);
        assertEquals(new BigDecimal("21000.00"), b.getTotalPrice());   // 7500 * 1.40 * 2
    }

    // ---------- overlap ----------

    @Test
    void overlap_detectsConflictsAndAllowsBackToBackStays() {
        Booking b = new Booking(guest, room, TODAY.plusDays(2), TODAY.plusDays(5));   // nights 12,13,14
        assertTrue(b.overlaps(TODAY.plusDays(1), TODAY.plusDays(3)));   // starts before, ends inside
        assertTrue(b.overlaps(TODAY.plusDays(4), TODAY.plusDays(8)));   // starts inside
        assertTrue(b.overlaps(TODAY.plusDays(3), TODAY.plusDays(4)));   // fully inside
        assertTrue(b.overlaps(TODAY, TODAY.plusDays(9)));               // fully contains
        assertFalse(b.overlaps(TODAY, TODAY.plusDays(2)));              // new guest leaves the day this one arrives
        assertFalse(b.overlaps(TODAY.plusDays(5), TODAY.plusDays(7)));  // new guest arrives the day this one leaves
    }

    // ---------- state machine ----------

    @Test
    void fullLifecycle_confirmedCheckedInCheckedOut_updatesRoom() {
        Booking b = new Booking(guest, room, TODAY, TODAY.plusDays(2));
        assertEquals(BookingStatus.CONFIRMED, b.getStatus());

        b.checkIn(TODAY.atTime(14, 0));
        assertEquals(BookingStatus.CHECKED_IN, b.getStatus());
        assertEquals(RoomStatus.OCCUPIED, room.getStatus());

        b.checkOut(TODAY.plusDays(2).atTime(11, 0));
        assertEquals(BookingStatus.CHECKED_OUT, b.getStatus());
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());
    }

    @Test
    void checkIn_beforeCheckInDate_isRejected() {
        Booking b = new Booking(guest, room, TODAY.plusDays(2), TODAY.plusDays(4));
        assertThrows(InvalidStateException.class, () -> b.checkIn(TODAY.atTime(10, 0)));
        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());
    }

    @Test
    void checkIn_afterStayEnded_isRejected() {
        Booking b = new Booking(guest, room, TODAY, TODAY.plusDays(2));
        assertThrows(InvalidStateException.class, () -> b.checkIn(TODAY.plusDays(2).atTime(9, 0)));
    }

    @Test
    void checkIn_whenRoomStillOccupied_isRejectedAndBookingUnchanged() {
        Guest other = new Guest("Isha Kapoor", "9000000002");
        Booking first = new Booking(guest, room, TODAY, TODAY.plusDays(1));
        first.checkIn(TODAY.atTime(12, 0));
        Booking second = new Booking(other, room, TODAY.plusDays(1), TODAY.plusDays(3));
        assertThrows(RoomUnavailableException.class, () -> second.checkIn(TODAY.plusDays(1).atTime(12, 0)));
        assertEquals(BookingStatus.CONFIRMED, second.getStatus());
    }

    @Test
    void invalidTransitions_areRejected() {
        Booking b = new Booking(guest, room, TODAY, TODAY.plusDays(2));
        assertThrows(InvalidStateException.class, () -> b.checkOut(TODAY.atTime(9, 0)));   // not checked in yet
        b.checkIn(TODAY.atTime(12, 0));
        assertThrows(InvalidStateException.class, b::cancel);                                // cannot cancel after check-in
        assertThrows(InvalidStateException.class, () -> b.checkIn(TODAY.atTime(13, 0)));    // cannot check in twice
        b.checkOut(TODAY.plusDays(1).atTime(10, 0));
        assertThrows(InvalidStateException.class, () -> b.checkOut(LocalDateTime.now()));   // cannot check out twice
    }

    @Test
    void cancel_releasesBlockOnlyForConfirmedBookings() {
        Booking b = new Booking(guest, room, TODAY.plusDays(5), TODAY.plusDays(6));
        b.cancel();
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        assertFalse(b.isActive());
        assertThrows(InvalidStateException.class, b::cancel);
        assertThrows(InvalidStateException.class, () -> b.checkIn(TODAY.plusDays(5).atTime(12, 0)));
    }

    @Test
    void payment_canBeMarkedOnce_andNotForCancelled() {
        Booking b = new Booking(guest, room, TODAY, TODAY.plusDays(1));
        assertEquals(PaymentStatus.PENDING, b.getPaymentStatus());
        b.markPaid();
        assertEquals(PaymentStatus.PAID, b.getPaymentStatus());
        assertThrows(InvalidStateException.class, b::markPaid);

        Booking c = new Booking(guest, room, TODAY.plusDays(3), TODAY.plusDays(4));
        c.cancel();
        assertThrows(InvalidStateException.class, c::markPaid);
    }

    @Test
    void roomMaintenance_cannotStartWhileOccupied() {
        Booking b = new Booking(guest, room, TODAY, TODAY.plusDays(1));
        b.checkIn(TODAY.atTime(12, 0));
        assertThrows(RoomUnavailableException.class, room::markMaintenance);
    }

    @Test
    void customExceptionHierarchy_catchableAsBookingException() {
        // One catch block can handle every kind of booking problem (inheritance of exceptions).
        try {
            new Booking(guest, room, TODAY, TODAY.plusDays(1)).checkOut(LocalDateTime.now());
        } catch (BookingException ex) {
            assertTrue(ex instanceof InvalidStateException);
            return;
        }
        throw new AssertionError("expected a BookingException");
    }
}
