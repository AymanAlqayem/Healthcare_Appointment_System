package org.example.healthcare_appointment_system.controller;

import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.AppointmentResponseDto;
import org.example.healthcare_appointment_system.dto.AvailabilitySlotResponseDto;
import org.example.healthcare_appointment_system.service.AppointmentService;
import org.example.healthcare_appointment_system.service.DoctorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

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















    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<AppointmentResponseDto> completeAppointment(@PathVariable Long id) {
        // No need to pass doctorId - it's automatically retrieved from security context
        AppointmentResponseDto updated = appointmentService.markAppointmentCompleted(id);
        return ResponseEntity.ok(updated);
    }

}