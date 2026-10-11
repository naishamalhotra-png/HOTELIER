package com.hotelier.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * OOPS: ABSTRACT CLASS. A Person is never created directly - only a Guest or an Employee is.
 * OOPS: ENCAPSULATION - fields are private; they are reached only through getters/setters.
 * OOPS: CONSTRUCTOR OVERLOADING - two constructors, one calling the other with this(...).
 */
@MappedSuperclass
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 120)
    private String email;

    /** Needed by JPA only. */
    protected Person() {
    }

    protected Person(String name, String phone) {
        this(name, phone, null);                       // 'this' calls the other constructor
    }

    protected Person(String name, String phone, String email) {
        this.name = requireText(name, "Name");         // 'this' refers to the object being built
        this.phone = requireText(phone, "Phone");
        this.email = email;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    /** OOPS: ABSTRACT METHOD - every subclass must say what kind of person it is. */
    public abstract String getRole();

    /** Overridden by subclasses (method overriding). */
    public String describe() {
        return getRole() + ": " + name + " (" + phone + ")";
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = requireText(name, "Name"); }
    public void setPhone(String phone) { this.phone = requireText(phone, "Phone"); }
    public void setEmail(String email) { this.email = email; }
}
