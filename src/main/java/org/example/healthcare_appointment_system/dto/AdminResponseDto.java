package org.example.healthcare_appointment_system.dto;

public record AdminResponseDto(
        Long id,
        String username,
        String email,
        String phone
) {
}
