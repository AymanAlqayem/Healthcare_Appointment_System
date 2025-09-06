package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.healthcare_appointment_system.entity.User;


import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    boolean existsByUser(User user);

    Optional<Doctor> findByUser(User user);
}

