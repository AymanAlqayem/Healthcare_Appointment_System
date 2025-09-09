package org.example.healthcare_appointment_system.dto;

import java.util.List;

public record MedicalHistoryResponseDto(
        Long patientId,
        String patientName,
        List<PrescriptionHistoryDto> prescriptions
) {}


