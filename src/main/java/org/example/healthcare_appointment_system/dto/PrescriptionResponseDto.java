package org.example.healthcare_appointment_system.dto;

import java.util.List;

public record PrescriptionResponseDto(
        String id,
        Long patientId,
        Long doctorId,
        Long appointmentId,
        String createdAt,
        String notes,
        List<String> medicines
) {
}
