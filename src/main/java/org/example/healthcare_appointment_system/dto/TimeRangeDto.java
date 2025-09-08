package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record TimeRangeDto(
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
) {}
