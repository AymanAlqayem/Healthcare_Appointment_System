package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByDoctorIdAndReservedFalse(Long doctorId);

    List<AvailabilitySlot> findByDoctorId(Long doctorId);

}

