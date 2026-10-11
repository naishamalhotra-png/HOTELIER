package com.hotelier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** OOPS: INHERITANCE - Guest IS-A Person. Also uses super(...) to reuse the parent constructor. */
@Entity
@Table(name = "guests")
public class Guest extends Person {

    @Column(unique = true, length = 20)
    private String guestCode;

    protected Guest() {
    }

    public Guest(String name, String phone) {
        super(name, phone);
    }

    public Guest(String name, String phone, String email) {
        super(name, phone, email);
    }

    /** OOPS: METHOD OVERRIDING of the abstract method declared in Person. */
    @Override
    public String getRole() {
        return "Guest";
    }

    /** Overrides describe() and extends it using super.describe(). */
    @Override
    public String describe() {
        return super.describe() + " [" + guestCode + "]";
    }

    public String getGuestCode() { return guestCode; }
    public void setGuestCode(String guestCode) { this.guestCode = guestCode; }
}
