package org.example.healthcare_appointment_system.dto;

import java.util.List;

public record PatientMedicalHistoryDto(
        Long patientId,
        String patientName,
        List<PrescriptionHistoryDto> prescriptions,
        List<MedicalRecordHistoryDto> medicalRecords
) {
}
