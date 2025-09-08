package org.example.healthcare_appointment_system.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilitySlotResponseDto(
        Long id,
        String date,       // formatted from LocalDate
        String startTime,  // formatted from LocalTime
        String endTime,    // formatted from LocalTime
        boolean reserved
) {}


