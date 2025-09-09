package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.AvailabilitySlot;
import org.example.healthcare_appointment_system.enums.WeekDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    boolean existsByDoctorIdAndDayOfWeekAndStartTimeAndEndTime(
            Long doctorId, WeekDay dayOfWeek, LocalTime startTime, LocalTime endTime);

    List<AvailabilitySlot> findByDoctorIdAndDayOfWeek(Long doctorId, WeekDay dayOfWeek);
}

