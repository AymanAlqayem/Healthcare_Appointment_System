package org.example.healthcare_appointment_system.dto;

public record AppointmentResponseDto(
        Long id,
        String doctorName,
        String patientName,
        String date,
        String startTime,
        String endTime,
        String status
) {}

