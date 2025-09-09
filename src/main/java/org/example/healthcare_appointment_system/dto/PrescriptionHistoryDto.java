package org.example.healthcare_appointment_system.dto;

import java.util.List;

public record PrescriptionHistoryDto(
        String appointmentDay,      // e.g. "MONDAY"
        String appointmentTime,     // e.g. "10:00 - 11:00"
        String createdAt,           // formatted date
        String notes,
        List<String> medicines
) {}

