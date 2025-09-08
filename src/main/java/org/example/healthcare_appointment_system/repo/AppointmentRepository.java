package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsBySlotId(Long slotId); // optional safety check
}

