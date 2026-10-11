package com.hotelier.dto;

import com.hotelier.model.Guest;

public record GuestResponse(Long id, String guestCode, String name, String phone, String email) {

    public static GuestResponse from(Guest guest) {
        return new GuestResponse(guest.getId(), guest.getGuestCode(), guest.getName(), guest.getPhone(), guest.getEmail());
    }
}
