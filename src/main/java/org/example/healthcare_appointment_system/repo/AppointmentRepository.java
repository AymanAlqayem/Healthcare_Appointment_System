package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.Appointment;
import org.example.healthcare_appointment_system.entity.Doctor;
import org.example.healthcare_appointment_system.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsBySlotId(Long slotId);

    List<Appointment> findByPatientId(Long patientId);

    Optional<Appointment> findByDoctorIdAndSlotId(Long doctorId, Long slotId);

    boolean existsBySlotIdAndStatus(Long slotId, AppointmentStatus status);

}

