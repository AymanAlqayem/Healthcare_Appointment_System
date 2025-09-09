package org.example.healthcare_appointment_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.*;
import org.example.healthcare_appointment_system.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments() {
        List<AppointmentResponseDto> appointments = appointmentService.getMyAppointments();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<List<AvailabilitySlotResponseDto>> getMyAvailableSlots() {
        List<AvailabilitySlotResponseDto> slots = appointmentService.getMyAvailableSlots();
        return ResponseEntity.ok(slots);
    }

    @PutMapping("/appointments/{appointmentId}/complete")
    public ResponseEntity<AppointmentResponseDto> completeAppointment(@PathVariable Long appointmentId) {
        AppointmentResponseDto updated = appointmentService.markAppointmentCompleted(appointmentId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/find-patient")
    public ResponseEntity<PatientResponseDto> getPatientId(@RequestParam String name) {
        return patientService.findPatient(name);
    }

    @PostMapping("/write-prescription/{patientId}")
    public ResponseEntity<PrescriptionResponseDto> writePrescription(
            @PathVariable Long patientId,
            @Valid @RequestBody PrescriptionDto dto
    ) {
        PrescriptionResponseDto response = prescriptionService.createPrescription(patientId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/medical-history/{patientId}")
    public ResponseEntity<MedicalHistoryResponseDto> getMedicalHistory(@PathVariable("patientId") Long patientId) {
        MedicalHistoryResponseDto response = medicalRecordService.getPatientMedicalHistory(patientId);
        return ResponseEntity.ok(response);
    }
}