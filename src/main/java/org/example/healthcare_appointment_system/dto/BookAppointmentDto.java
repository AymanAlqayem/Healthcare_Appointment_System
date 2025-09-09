package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.NotNull;

public record BookAppointmentDto(
        @NotNull(message = "doctor id can not be null")
        Long doctorId,

        @NotNull(message = "slot id can not be null")
        Long slotId
) {
}

