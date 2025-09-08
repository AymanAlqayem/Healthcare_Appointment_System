package org.example.healthcare_appointment_system.dto;


import jakarta.validation.constraints.NotNull;

public record AppointmentDto(
        @NotNull Long doctorId,
        @NotNull Long patientId,
        @NotNull Long slotId
) {
}
