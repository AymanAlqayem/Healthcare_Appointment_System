package org.example.healthcare_appointment_system.dto;

public record AuthResponse(String accessToken,
                           long expiresInMillis,
                           String refreshToken) {
}
