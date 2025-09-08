package org.example.healthcare_appointment_system.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilitySlotResponseDto(
        Long id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        boolean reserved
) { }

