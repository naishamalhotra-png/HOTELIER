package com.hotelier.service;

import com.hotelier.dto.BookingResponse;
import com.hotelier.dto.DashboardResponse;
import com.hotelier.dto.StaffResponse;
import com.hotelier.model.Booking;
import com.hotelier.model.BookingStatus;
import com.hotelier.model.Employee;
import com.hotelier.model.PaymentStatus;
import com.hotelier.model.RoomStatus;
import com.hotelier.repository.ReceptionistRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Builds the dashboard numbers from the real database (nothing is hard-coded). */
@Service
public class HotelService {

    private final RoomService roomService;
    private final GuestService guestService;
    private final BookingService bookingService;
    private final ReceptionistRepository receptionistRepository;

    public HotelService(RoomService roomService, GuestService guestService, BookingService bookingService,
                        ReceptionistRepository receptionistRepository) {
        this.roomService = roomService;
        this.guestService = guestService;
        this.bookingService = bookingService;
        this.receptionistRepository = receptionistRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        int total = (int) roomService.count();
        int available = (int) roomService.countByStatus(RoomStatus.AVAILABLE);
        int occupied = (int) roomService.countByStatus(RoomStatus.OCCUPIED);
        int maintenance = (int) roomService.countByStatus(RoomStatus.MAINTENANCE);
        int occupancy = total == 0 ? 0 : Math.round(occupied * 100f / total);

        List<Booking> active = new ArrayList<>(bookingService.findActive());
        active.sort(Comparator.comparing(Booking::getCheckInDate).thenComparing(Booking::getId));
        int checkedIn = (int) bookingService.countByStatus(BookingStatus.CHECKED_IN);
        int upcoming = (int) bookingService.countByStatus(BookingStatus.CONFIRMED);

        BigDecimal revenue = BigDecimal.ZERO;
        for (Booking b : bookingService.findAll()) {
            if (b.getPaymentStatus() == PaymentStatus.PAID && b.getStatus() != BookingStatus.CANCELLED) {
                revenue = revenue.add(b.getTotalPrice());
            }
        }

        List<BookingResponse> current = active.stream().limit(6).map(BookingResponse::from).toList();

        // Polymorphism: a List<Employee> filled with Receptionist objects; each answers getRole()/getDuty() itself.
        List<Employee> staff = new ArrayList<>(receptionistRepository.findAll());
        List<StaffResponse> staffOnDesk = staff.stream().map(StaffResponse::from).toList();

        return new DashboardResponse(total, available, occupied, maintenance, occupancy,
                (int) guestService.count(), active.size(), checkedIn, upcoming, revenue, current, staffOnDesk);
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaff() {
        List<Employee> staff = new ArrayList<>(receptionistRepository.findAll());
        return staff.stream().map(StaffResponse::from).toList();
    }
}
