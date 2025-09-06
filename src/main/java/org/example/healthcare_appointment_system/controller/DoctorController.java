package org.example.healthcare_appointment_system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/me")
    public String me() {
        return "Doctor portal";
    }
}