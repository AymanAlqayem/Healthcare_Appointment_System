package org.example.healthcare_appointment_system.controller;

import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.AvailabilitySlotResponseDto;
import org.example.healthcare_appointment_system.service.AppointmentService;
import org.example.healthcare_appointment_system.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @GetMapping("/doctor/{id}/slots")
    public ResponseEntity<List<AvailabilitySlotResponseDto>> getAvailableSlots(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(id));
    }


}