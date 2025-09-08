package org.example.healthcare_appointment_system.dto;

import org.example.healthcare_appointment_system.entity.AvailabilitySlot;

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
