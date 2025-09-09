package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record DoctorDto(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100)
        String password,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")
        String phone,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Specialty is required")
        String specialty,

        List<DaySlotsCreateDto> slots
) {
}
