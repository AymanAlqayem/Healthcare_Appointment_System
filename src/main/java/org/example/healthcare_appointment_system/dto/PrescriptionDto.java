package org.example.healthcare_appointment_system.dto;

import java.util.List;

public record PrescriptionDto(
        Long appointmentId,
        String notes,
        List<String> medicines
) {
}
