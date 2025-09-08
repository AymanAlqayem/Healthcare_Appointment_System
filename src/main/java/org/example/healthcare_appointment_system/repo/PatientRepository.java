package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.Patient;
import org.example.healthcare_appointment_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByUser(User user);

    Optional<Patient> findByUser(User user);

    Optional<Patient> findByUserId(Long userId);
}