package com.hotelier.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * OOPS: MULTILEVEL INHERITANCE - Person -> Employee -> Receptionist.
 * Employee is abstract too: it adds shared staff data but leaves getDuty() to its children.
 */
@MappedSuperclass
public abstract class Employee extends Person {

    @Column(length = 20)
    private String shift;

    protected Employee() {
    }

    protected Employee(String name, String phone, String email, String shift) {
        super(name, phone, email);
        this.shift = shift;
    }

    public abstract String getDuty();

    @Override
    public String describe() {
        return super.describe() + " - " + shift + " shift";
    }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
}
