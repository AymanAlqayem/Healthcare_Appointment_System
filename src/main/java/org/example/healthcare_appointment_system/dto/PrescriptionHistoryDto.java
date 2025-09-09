package org.example.healthcare_appointment_system.dto;

import java.util.List;

public record PrescriptionHistoryDto(
        String appointmentDay,
        String appointmentTime,
        String createdAt,
        String notes,
        List<String> medicines
) {
}

