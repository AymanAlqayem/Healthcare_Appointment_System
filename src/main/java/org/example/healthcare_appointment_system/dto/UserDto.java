package org.example.healthcare_appointment_system.dto;

import java.util.Set;

public record UserDto(Long id, String username, Set<String> roles) {

}