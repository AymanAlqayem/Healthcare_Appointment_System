package org.example.healthcare_appointment_system.dto;

public record DoctorResponseDto(
        Long id,
        String username,
        String email,
        String phone,
        String specialty
) {
}
