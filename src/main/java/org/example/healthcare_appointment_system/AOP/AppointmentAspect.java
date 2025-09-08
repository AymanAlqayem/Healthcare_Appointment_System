package org.example.healthcare_appointment_system.AOP;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.healthcare_appointment_system.dto.BookAppointmentDto;
import org.example.healthcare_appointment_system.entity.Appointment;
import org.example.healthcare_appointment_system.entity.AvailabilitySlot;
import org.example.healthcare_appointment_system.repo.AppointmentRepository;
import org.example.healthcare_appointment_system.repo.AvailabilitySlotRepository;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AppointmentAspect {
    private final AvailabilitySlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;

    @Before("@annotation(BookAppointmentCheck) && args(dto,..)")
    public void checkSlotAvailability(BookAppointmentDto dto) {
        AvailabilitySlot slot = slotRepository.findById(dto.slotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.isReserved()) {
            throw new RuntimeException("This slot is already booked");
        }
    }

    @AfterReturning(pointcut = "@annotation(BookAppointmentCheck)", returning = "result")
    public void markSlotAsReserved(Object result) {
        if (result instanceof Appointment appointment) {
            AvailabilitySlot slot = appointment.getSlot();
            slot.setReserved(true);
            slotRepository.save(slot);
        }
    }

    @Before("@annotation(CancelAppointmentCheck) && args(appointmentId,..)")
    public void freeSlotBeforeCancel(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Free the slot
        AvailabilitySlot slot = appointment.getSlot();
        slot.setReserved(false);
        // Save the slot
        slot.getDoctor().getAvailabilitySlots().stream()
                .filter(s -> s.getId().equals(slot.getId()))
                .findFirst()
                .ifPresent(s -> s.setReserved(false));
    }
}

