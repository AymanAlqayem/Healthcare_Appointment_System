package org.example.healthcare_appointment_system.repo;

import jakarta.validation.constraints.NotNull;
import org.example.healthcare_appointment_system.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Optional: Find available slots for a specific doctor and date
    List<AvailabilitySlot> findByDoctorIdAndDateAndReservedFalse(Long doctorId, LocalDate date);

    // Optional: Find available slots for a specific doctor that are in the future
    @Query("SELECT s FROM AvailabilitySlot s WHERE s.doctor.id = :doctorId AND s.reserved = false AND (s.date > CURRENT_DATE OR (s.date = CURRENT_DATE AND s.startTime > CURRENT_TIME))")
    List<AvailabilitySlot> findFutureAvailableSlotsByDoctorId(@Param("doctorId") Long doctorId);

}

