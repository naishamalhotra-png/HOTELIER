package com.hotelier.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(int totalRooms, int availableRooms, int occupiedRooms, int maintenanceRooms,
                                int occupancyPercent, int totalGuests, int activeBookings, int checkedInNow,
                                int upcomingArrivals, BigDecimal paidRevenue,
                                List<BookingResponse> currentBookings, List<StaffResponse> staffOnDesk) {
}
