package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.NotNull;

public record BookAppointmentDto(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        @NotNull Long slotId
) { }

