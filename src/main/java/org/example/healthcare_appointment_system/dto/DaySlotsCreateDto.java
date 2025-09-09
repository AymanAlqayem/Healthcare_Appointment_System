package org.example.healthcare_appointment_system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.example.healthcare_appointment_system.enums.WeekDay;

import java.util.List;

public record DaySlotsCreateDto(
        @NotNull(message = "day can not be null")
        WeekDay dayOfWeek,

        @NotEmpty(message = "Slots are required")
        List<@Valid TimeRangeDto> slots
) {
}
