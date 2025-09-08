package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.healthcare_appointment_system.entity.User;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    boolean existsByUser(User user);

    Optional<Doctor> findByUser(User user);

    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}

