package org.example.healthcare_appointment_system.dto;

import jakarta.validation.constraints.*;
import org.example.healthcare_appointment_system.enums.Role;

import java.util.Set;

//public record RegisterRequest(
//        @NotBlank String username,
//        @NotBlank @Size(min = 6) String password,
//        @NotEmpty Set<String> roles // ["ADMIN", "DOCTOR", "PATIENT"]
//) {}


//public record RegisterRequest(
//        @NotBlank(message = "Username cannot be empty")
//        String username,
//
//        @NotBlank(message = "Password cannot be empty")
//        @Size(min = 6, message = "Password must be at least 6 characters")
//        String password,
//
//        @Email(message = "Email should be valid")
//        String email,
//
//        String phone,
//
//        Set<Role> roles
//) {}

public record RegisterRequest(
        String username,
        String password,
        String email,
        String phone,
        Set<Role> roles,
        String specialty,   // for doctors
        String gender,      // for patients
        String dateOfBirth  // for patients
) {}
