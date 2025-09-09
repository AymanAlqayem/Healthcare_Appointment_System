package org.example.healthcare_appointment_system.dto;

import java.util.List;

public record DoctorResponseDto(
        Long id,
        String username,
        String email,
        String phone,
        String specialty,
        List<AvailabilitySlotResponseDto> availabilitySlots
) {
}
