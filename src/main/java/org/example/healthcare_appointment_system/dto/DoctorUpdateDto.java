package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DoctorUpdateDto(
        @NotNull(message = "Doctor ID is required")
        Long id,

        @NotBlank(message = "Specialty is required")
        String specialty,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")
        String phone,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}

