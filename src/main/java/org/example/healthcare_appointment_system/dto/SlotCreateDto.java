package org.example.healthcare_appointment_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import org.example.healthcare_appointment_system.enums.WeekDay;

import java.time.LocalDate;
import java.time.LocalTime;

public record SlotCreateDto(
        @NotNull(message = "day is required")
        WeekDay dayOfWeek,

        @NotNull(message = "Start time is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime endTime
) {
}
