package org.example.healthcare_appointment_system.AOP;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AppointmentLoggingAspect {
    // After booking an appointment successfully
    @AfterReturning(
            pointcut = "execution(* org.example.healthcare_appointment_system.service.AppointmentService.bookAppointment(..))",
            returning = "response"
    )
    public void logAppointmentBooking(Object response) {
        log.info("Appointment booked: {}", response);
    }

    // After cancelling an appointment successfully
    @AfterReturning(
            pointcut = "execution(* org.example.healthcare_appointment_system.service.AppointmentService.cancelAppointment(..))",
            returning = "response"
    )
    public void logAppointmentCancellation(Object response) {
        log.info("Appointment cancelled: {}", response);
    }
}

