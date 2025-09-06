package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.*;

import java.util.Set;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 6) String password,
        @NotEmpty Set<String> roles // ["ADMIN", "DOCTOR", "PATIENT"]
) {}