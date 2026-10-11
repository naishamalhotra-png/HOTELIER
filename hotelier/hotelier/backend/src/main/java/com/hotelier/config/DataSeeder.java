package com.hotelier.config;

import com.hotelier.dto.BookingRequest;
import com.hotelier.dto.GuestRequest;
import com.hotelier.dto.RoomRequest;
import com.hotelier.model.Guest;
import com.hotelier.model.Receptionist;
import com.hotelier.model.Room;
import com.hotelier.repository.ReceptionistRepository;
import com.hotelier.repository.RoomRepository;
import com.hotelier.service.BookingService;
import com.hotelier.service.GuestService;
import com.hotelier.service.RoomService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Loads fictional sample data the first time the app starts on an empty database. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoomRepository roomRepository;
    private final ReceptionistRepository receptionistRepository;
    private final RoomService roomService;
    private final GuestService guestService;
    private final BookingService bookingService;
    private final Clock clock;
    private final boolean enabled;

    public DataSeeder(RoomRepository roomRepository, ReceptionistRepository receptionistRepository,
                      RoomService roomService, GuestService guestService, BookingService bookingService,
                      Clock clock, @Value("${hotelier.seed-demo-data:true}") boolean enabled) {
        this.roomRepository = roomRepository;
        this.receptionistRepository = receptionistRepository;
        this.roomService = roomService;
        this.guestService = guestService;
        this.bookingService = bookingService;
        this.clock = clock;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled || roomRepository.count() > 0) {
            return;
        }
        try {
            seedStaff();
            seedRooms();
            seedGuestsAndBookings();
            log.info("Sample data loaded.");
        } catch (Exception ex) {
            // A seeding problem must never stop the whole application from starting.
            log.warn("Sample data could not be loaded: {}", ex.getMessage());
        }
    }

    private void seedStaff() {
        receptionistRepository.save(new Receptionist("Meera Nair", "9000000101", "meera@hotelier.example", "Morning"));
        receptionistRepository.save(new Receptionist("Arjun Verma", "9000000102", "arjun@hotelier.example", "Evening"));
    }

    private void seedRooms() {
        for (String number : new String[]{"101", "102", "103", "104"}) {
            roomService.create(new RoomRequest(number, "STANDARD", 2, new BigDecimal("2500"), null));
        }
        for (String number : new String[]{"201", "202", "203", "204"}) {
            roomService.create(new RoomRequest(number, "DELUXE", 3, new BigDecimal("3800"), null));
        }
        roomService.create(new RoomRequest("301", "SUITE", 4, new BigDecimal("7500"), null));
        roomService.create(new RoomRequest("302", "SUITE", 4, new BigDecimal("8200"), null));
    }

    private void seedGuestsAndBookings() {
        Guest aarav = guestService.create(new GuestRequest("Aarav Mehta", "9000000001", "aarav@example.com"));
        Guest isha = guestService.create(new GuestRequest("Isha Kapoor", "9000000002", "isha@example.com"));
        Guest rohan = guestService.create(new GuestRequest("Rohan Iyer", "9000000003", null));
        guestService.create(new GuestRequest("Sana Qureshi", "9000000004", "sana@example.com"));

        LocalDate today = LocalDate.now(clock);
        Room r101 = roomRepository.findByRoomNumber("101").orElseThrow();
        Room r201 = roomRepository.findByRoomNumber("201").orElseThrow();
        Room r301 = roomRepository.findByRoomNumber("301").orElseThrow();

        // A guest staying right now (booked for today, then checked in).
        Long stay = bookingService.createBooking(
                new BookingRequest(aarav.getId(), r101.getId(), today, today.plusDays(2), 2)).getId();
        bookingService.checkIn(stay);
        // Two upcoming reservations.
        bookingService.createBooking(new BookingRequest(isha.getId(), r201.getId(), today.plusDays(3), today.plusDays(6), 2));
        bookingService.createBooking(new BookingRequest(rohan.getId(), r301.getId(), today.plusDays(7), today.plusDays(9), 3));
    }
}
