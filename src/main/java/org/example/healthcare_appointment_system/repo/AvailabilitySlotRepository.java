package org.example.healthcare_appointment_system.repo;

import jakarta.validation.constraints.NotNull;
import org.example.healthcare_appointment_system.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByDoctorIdAndReservedFalse(Long doctorId);

    List<AvailabilitySlot> findByDoctorId(Long doctorId);

    boolean existsByDoctorIdAndDateAndStartTimeAndEndTime(
            Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime
    );

    List<AvailabilitySlot> findByDoctorIdAndDate(Long doctorId, LocalDate date);


}

