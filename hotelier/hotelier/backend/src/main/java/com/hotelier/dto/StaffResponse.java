package com.hotelier.dto;

import com.hotelier.model.Employee;

public record StaffResponse(Long id, String name, String role, String shift, String duty) {

    /** Works for ANY Employee subclass - getRole() and getDuty() run the child's version (polymorphism). */
    public static StaffResponse from(Employee employee) {
        return new StaffResponse(employee.getId(), employee.getName(), employee.getRole(),
                employee.getShift(), employee.getDuty());
    }
}
