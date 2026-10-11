package com.hotelier.dto;

/** JSON body for creating or editing a guest. */
public record GuestRequest(String name, String phone, String email) {
}
