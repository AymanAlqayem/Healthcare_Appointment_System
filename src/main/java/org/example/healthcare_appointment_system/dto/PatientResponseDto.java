package org.example.healthcare_appointment_system.dto;

public record PatientResponseDto(
        Long id,
        Long userId,
        String username,
        String email,
        String gender,
        String dateOfBirth
) {
}