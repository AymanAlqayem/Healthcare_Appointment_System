package org.example.healthcare_appointment_system.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.AOP.BookAppointmentCheck;
import org.example.healthcare_appointment_system.AOP.CancelAppointmentCheck;
import org.example.healthcare_appointment_system.dto.AppointmentDto;
import org.example.healthcare_appointment_system.dto.AppointmentResponseDto;
import org.example.healthcare_appointment_system.dto.AvailabilitySlotResponseDto;
import org.example.healthcare_appointment_system.dto.BookAppointmentDto;
import org.example.healthcare_appointment_system.entity.Appointment;
import org.example.healthcare_appointment_system.entity.AvailabilitySlot;
import org.example.healthcare_appointment_system.entity.Doctor;
import org.example.healthcare_appointment_system.entity.Patient;
import org.example.healthcare_appointment_system.enums.AppointmentStatus;
import org.example.healthcare_appointment_system.repo.AppointmentRepository;
import org.example.healthcare_appointment_system.repo.AvailabilitySlotRepository;
import org.example.healthcare_appointment_system.repo.DoctorRepository;
import org.example.healthcare_appointment_system.repo.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AvailabilitySlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public List<AvailabilitySlotResponseDto> getAvailableSlots(Long doctorId) {
        return slotRepository.findByDoctorId(doctorId)
                .stream()
                .map(slot -> new AvailabilitySlotResponseDto(
                        slot.getId(),
                        slot.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),  // LocalDate → String
                        slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")), // LocalTime → String
                        slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),   // LocalTime → String
                        slot.isReserved()
                ))
                .toList();
    }

    @BookAppointmentCheck
    @Transactional
    public AppointmentResponseDto bookAppointment(BookAppointmentDto dto) {

        if (appointmentRepository.existsBySlotIdAndStatus(dto.slotId(), AppointmentStatus.BOOKED)) {
            throw new IllegalStateException("This slot is already booked");
        }

        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        AvailabilitySlot slot = slotRepository.findById(dto.slotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // Prevent double booking
        if (slot.isReserved()) {
            throw new RuntimeException("This slot is already booked");
        }

        // Mark the slot as reserved
        slot.setReserved(true);
        slotRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setAppointmentTime(LocalDateTime.of(slot.getDate(), slot.getStartTime()));
        appointment.setStatus(AppointmentStatus.BOOKED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Convert to response DTO
        return new AppointmentResponseDto(
                savedAppointment.getId(),
                savedAppointment.getDoctor().getUser().getUsername(),
                savedAppointment.getPatient().getUser().getUsername(),
                savedAppointment.getSlot().getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                savedAppointment.getSlot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                savedAppointment.getSlot().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                savedAppointment.getStatus().name()
        );
    }
}
