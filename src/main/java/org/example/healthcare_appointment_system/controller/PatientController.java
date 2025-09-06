package org.example.healthcare_appointment_system.controller;

import jakarta.validation.Valid;
import org.example.healthcare_appointment_system.dto.PatientDto;
import org.example.healthcare_appointment_system.service.PatientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // Anyone with PATIENT/DOCTOR/ADMIN (configured in SecurityConfig) can see the list
    @GetMapping
    public List<PatientDto> list() {
        return patientService.list();
    }

    @GetMapping("/{id}")
    public PatientDto get(@PathVariable Long id) {
        return patientService.get(id);
    }

    // Only DOCTOR or ADMIN can create/update/delete patients
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping
    public PatientDto create(@Valid @RequestBody PatientDto dto) {
        return patientService.create(dto);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/{id}")
    public PatientDto update(@PathVariable Long id, @Valid @RequestBody PatientDto dto) {
        return patientService.update(id, dto);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        patientService.delete(id);
    }
}