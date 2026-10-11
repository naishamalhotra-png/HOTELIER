package com.hotelier.controller;

import com.hotelier.dto.DashboardResponse;
import com.hotelier.dto.StaffResponse;
import com.hotelier.service.HotelService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final HotelService hotelService;

    public DashboardController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return hotelService.getDashboard();
    }

    @GetMapping("/staff")
    public List<StaffResponse> staff() {
        return hotelService.getStaff();
    }
}
