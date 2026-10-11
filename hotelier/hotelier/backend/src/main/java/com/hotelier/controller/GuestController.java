package com.hotelier.controller;

import com.hotelier.dto.GuestRequest;
import com.hotelier.dto.GuestResponse;
import com.hotelier.service.GuestService;
import java.util.List;
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

/** Controllers only translate HTTP <-> Java. All rules are in the service. */
@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @GetMapping
    public List<GuestResponse> list(@RequestParam(required = false) String q) {
        return guestService.search(q).stream().map(GuestResponse::from).toList();
    }

    @GetMapping("/{id}")
    public GuestResponse get(@PathVariable Long id) {
        return GuestResponse.from(guestService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse create(@RequestBody GuestRequest request) {
        return GuestResponse.from(guestService.create(request));
    }

    @PutMapping("/{id}")
    public GuestResponse update(@PathVariable Long id, @RequestBody GuestRequest request) {
        return GuestResponse.from(guestService.update(id, request));
    }
}
