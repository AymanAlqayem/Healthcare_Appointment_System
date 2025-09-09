package org.example.healthcare_appointment_system.dto;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String path
) {
}