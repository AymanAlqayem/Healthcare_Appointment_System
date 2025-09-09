package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.*;

public record AuthRequest(
        @NotBlank(message = "Username cannot be empty")
        String username,

        @NotBlank(message = "password cannot be empty")
        String password
) {
}