package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {}