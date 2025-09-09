package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);

    Optional<Doctor> findByUserId(Long userId);
}

