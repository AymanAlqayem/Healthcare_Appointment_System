package org.example.healthcare_appointment_system.dto;

import java.util.List;
import java.util.Map;

public record MedicalRecordHistoryDto(
        String createdAt,
        String notes,
        List<String> attachments,
        Map<String, String> labResults
) {}
