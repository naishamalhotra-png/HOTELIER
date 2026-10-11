package com.hotelier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hotelier.model.DeluxeRoom;
import com.hotelier.model.Employee;
import com.hotelier.model.Guest;
import com.hotelier.model.Person;
import com.hotelier.model.Receptionist;
import com.hotelier.model.Room;
import com.hotelier.model.StandardRoom;
import com.hotelier.model.Suite;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Proves the OOPS concepts really work (and are not just decoration). */
class OopsConceptsTest {

    @Test
    void runtimePolymorphism_parentReferenceCallsChildOverride() {
        Room standard = new StandardRoom("101", 2, new BigDecimal("1000"));
        Room deluxe = new DeluxeRoom("201", 3, new BigDecimal("1000"));
        Room suite = new Suite("301", 4, new BigDecimal("1000"));

        // Same call, same parent type - three different answers decided at run time.
        assertEquals(new BigDecimal("1000.00"), standard.nightlyRate());
        assertEquals(new BigDecimal("1150.00"), deluxe.nightlyRate());
        assertEquals(new BigDecimal("1400.00"), suite.nightlyRate());
        assertEquals("Suite", suite.getRoomType());
    }

    @Test
    void methodOverloading_bothCalculateCostVersionsAgree() {
        Room deluxe = new DeluxeRoom("201", 3, new BigDecimal("1000"));
        BigDecimal byNights = deluxe.calculateCost(3);
        BigDecimal byDates = deluxe.calculateCost(LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 13));
        assertEquals(new BigDecimal("3450.00"), byNights);
        assertEquals(byNights, byDates);
    }

    @Test
    void multilevelInheritance_personEmployeeReceptionist() {
        Receptionist r = new Receptionist("Meera Nair", "9000000101", "m@x.com", "Morning");
        assertInstanceOf(Employee.class, r);
        assertInstanceOf(Person.class, r);
        Person asPerson = r;                              // parent reference to child object
        assertEquals("Receptionist", asPerson.getRole()); // child's override runs
        assertTrue(asPerson.describe().contains("Morning shift"));
    }

    @Test
    void overridingAndSuper_guestDescribeExtendsParent() {
        Guest g = new Guest("Aarav Mehta", "9000000001");
        g.setGuestCode("G-1001");
        assertEquals("Guest: Aarav Mehta (9000000001) [G-1001]", g.describe());
    }

    @Test
    void constructorOverloading_withAndWithoutEmail() {
        Guest a = new Guest("A Person", "9000000001");
        Guest b = new Guest("B Person", "9000000002", "b@example.com");
        assertEquals(null, a.getEmail());
        assertEquals("b@example.com", b.getEmail());
    }

    @Test
    void constructorsRejectInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> new Guest(" ", "9000000001"));
        assertThrows(IllegalArgumentException.class, () -> new StandardRoom("101", 0, new BigDecimal("100")));
        assertThrows(IllegalArgumentException.class, () -> new StandardRoom("101", 2, BigDecimal.ZERO));
    }

    @Test
    void polymorphicList_everyEmployeeAnswersForItself() {
        List<Employee> staff = List.of(new Receptionist("A Name", "9000000001", null, "Day"));
        for (Employee e : staff) {
            assertEquals("Handles check-in, check-out and billing", e.getDuty());
        }
    }
}
