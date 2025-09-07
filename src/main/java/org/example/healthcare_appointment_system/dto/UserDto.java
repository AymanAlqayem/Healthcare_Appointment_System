package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.NotBlank;
import org.example.healthcare_appointment_system.enums.Role;

import java.util.Set;

public record UserDto(
        @NotBlank(message = "Username can not be empty")
        String username,
        String email,
        String password,
        Role role
) {
}
