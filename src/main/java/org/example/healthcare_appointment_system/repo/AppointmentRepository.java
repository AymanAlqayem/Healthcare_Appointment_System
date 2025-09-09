package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.Appointment;
import org.example.healthcare_appointment_system.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    boolean existsBySlotIdAndStatus(Long slotId, AppointmentStatus status);

}

