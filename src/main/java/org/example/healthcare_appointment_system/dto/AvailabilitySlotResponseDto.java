package org.example.healthcare_appointment_system.dto;

public record AvailabilitySlotResponseDto(
        Long id,
        String date,
        String startTime,
        String endTime,
        boolean reserved
) {
}


