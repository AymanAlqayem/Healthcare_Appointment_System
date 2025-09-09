package org.example.healthcare_appointment_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.*;
import org.example.healthcare_appointment_system.entity.Appointment;
import org.example.healthcare_appointment_system.service.AppointmentService;
import org.example.healthcare_appointment_system.service.DoctorService;
import org.example.healthcare_appointment_system.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @GetMapping("/search-doctor")
    public ResponseEntity<List<DoctorResponseDto>> searchBySpecialty(@RequestParam String specialty) {
        List<DoctorResponseDto> response = doctorService.searchBySpecialty(specialty);
        return ResponseEntity.ok(response);
    }

    @PutMapping("update-info")
    public ResponseEntity<PatientResponseDto> updateInfo(@RequestBody @Valid PatientUpdateDto dto) {
        PatientResponseDto updatedPatient = patientService.updateInfo(dto);
        return ResponseEntity.ok(updatedPatient);
    }

    @PostMapping("/book-appointment")
    public ResponseEntity<AppointmentResponseDto> bookAppointment(
            @RequestBody @Valid BookAppointmentDto dto
    ) {
        AppointmentResponseDto response = appointmentService.bookAppointment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments() {
        // No need for @RequestParam - automatically gets current patient's appointments
        List<AppointmentResponseDto> appointments = patientService.getMyAppointments();
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/cancel-appointment/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(@PathVariable Long id) {
        AppointmentResponseDto response = patientService.cancelAppointment(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("medical-history")
    public ResponseEntity<PatientMedicalHistoryDto> getMyMedicalHistory() {
        PatientMedicalHistoryDto response = patientService.getPatientHistory();
        return ResponseEntity.ok(response);
    }
}