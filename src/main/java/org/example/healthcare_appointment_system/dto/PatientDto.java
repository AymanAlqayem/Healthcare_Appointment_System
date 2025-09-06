package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.*;

public record PatientDto(
        Long id,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone,
        @Email String email
) {
}
