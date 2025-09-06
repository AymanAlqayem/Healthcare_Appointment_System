package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.*;

public record AuthRequest(
        @NotBlank String username,
        @NotBlank String password
) {


}