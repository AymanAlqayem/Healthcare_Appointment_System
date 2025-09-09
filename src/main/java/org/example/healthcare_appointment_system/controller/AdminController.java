package org.example.healthcare_appointment_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.*;
import org.example.healthcare_appointment_system.repo.DoctorRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.example.healthcare_appointment_system.service.AdminService;
import org.example.healthcare_appointment_system.service.DoctorService;
import org.example.healthcare_appointment_system.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    @PostMapping("/create-admin")
    public ResponseEntity<AdminResponseDto> createAdmin(@Valid @RequestBody AdminDto dto) {
        AdminResponseDto response = adminService.createAdmin(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Doctor APIs.
     */
    @PostMapping("/create-doctor")
    public ResponseEntity<DoctorResponseDto> createDoctor(@Valid @RequestBody DoctorDto dto) {
        DoctorResponseDto response = doctorService.createDoctor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<String> deleteDoctor(@PathVariable Long id) {
        return doctorService.deleteDoctor(id);
    }

    @PutMapping("update-doctor")
    public ResponseEntity<DoctorResponseDto> updateDoctor(@RequestBody @Valid DoctorUpdateDto dto) {
        DoctorResponseDto updatedDoctor = doctorService.updateDoctor(dto);
        return ResponseEntity.ok(updatedDoctor);
    }

    @PostMapping("/doctor/{doctorId}/add-slots")
    public ResponseEntity<List<SlotResponseDto>> addDaySlots(
            @PathVariable Long doctorId,
            @Valid @RequestBody DaySlotsCreateDto dto
    ) {
        List<SlotResponseDto> response = adminService.addDaySlots(doctorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/doctors")
    public List<DoctorResponseDto> getDoctors() {
        return doctorService.getAllDoctors();
    }

    /**
     * Patient APIs
     */
    @PostMapping("/create-patient")
    public ResponseEntity<PatientResponseDto> createPatient(@Valid @RequestBody PatientDto dto) {
        PatientResponseDto response = patientService.createPatient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/patients/{id}")
    public ResponseEntity<String> deletePatient(@PathVariable Long id) {
        return patientService.deletePatient(id);
    }

    @GetMapping("/patients")
    public List<PatientResponseDto> getPatients() {
        return patientService.getAllPatients();
    }
}


