package com.hotelier.controller;

import com.hotelier.dto.RoomRequest;
import com.hotelier.dto.RoomResponse;
import com.hotelier.service.RoomService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponse> list(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) Integer minCapacity) {
        return roomService.search(q, type, status, minCapacity).stream().map(RoomResponse::from).toList();
    }

    @GetMapping("/available")
    public List<RoomResponse> available(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer guests) {
        return roomService.findAvailableForDates(checkIn, checkOut, guests).stream().map(RoomResponse::from).toList();
    }

    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable Long id) {
        return RoomResponse.from(roomService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@RequestBody RoomRequest request) {
        return RoomResponse.from(roomService.create(request));
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @RequestBody RoomRequest request) {
        return RoomResponse.from(roomService.update(id, request));
    }
}
