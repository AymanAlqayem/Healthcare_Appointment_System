package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.healthcare_appointment_system.enums.Role;

public record UserDto(
        @NotBlank(message = "Username can not be empty")
        String username,

        @Email(message = "Email is required")
        String email,

        String password,

        @NotNull(message = "Role is required")
        Role role
) {
}
