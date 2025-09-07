package org.example.healthcare_appointment_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import org.example.healthcare_appointment_system.enums.Gender;

import java.time.LocalDate;

public record PatientDto(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100)
        String password,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")
        String phone,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotNull(message = "Gender is required")  // <-- change here
        Gender gender,

        @NotNull(message = "Date of Birth is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        LocalDate dateOfBirth
) {
}



