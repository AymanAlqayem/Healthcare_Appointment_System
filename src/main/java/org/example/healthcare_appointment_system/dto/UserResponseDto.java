package org.example.healthcare_appointment_system.dto;

import org.example.healthcare_appointment_system.enums.Role;

import java.util.Set;

// User response (without password)
public record UserResponseDto(
        Long id,
        String username,
        String email,
        Set<Role> roles
) {}