package org.example.healthcare_appointment_system.dto;

public record DoctorResponseDto(
        Long id,
        Long userId,
        String username,
        String email,
        String specialty
) {}
