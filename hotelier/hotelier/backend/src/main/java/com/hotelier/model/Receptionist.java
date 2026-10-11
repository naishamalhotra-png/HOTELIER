package com.hotelier.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Third level of the hierarchy: Person -> Employee -> Receptionist. */
@Entity
@Table(name = "receptionists")
public class Receptionist extends Employee {

    protected Receptionist() {
    }

    public Receptionist(String name, String phone, String email, String shift) {
        super(name, phone, email, shift);
    }

    @Override
    public String getRole() {
        return "Receptionist";
    }

    @Override
    public String getDuty() {
        return "Handles check-in, check-out and billing";
    }
}
