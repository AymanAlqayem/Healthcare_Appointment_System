package org.example.healthcare_appointment_system.dto;

import org.example.healthcare_appointment_system.enums.Role;

import java.util.Set;

public record UserResponseDto(
        Long id,
        String username,
        String email,
        Role role
) {
}