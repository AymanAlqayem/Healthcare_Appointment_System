package org.example.healthcare_appointment_system.dto;

import org.example.healthcare_appointment_system.enums.Role;

import java.util.Set;

public record RegisterRequest(
        String username,
        String password,
        String email,
        String phone,
        Set<Role> roles,
        String specialty,
        String gender,
        String dateOfBirth
) {
}
