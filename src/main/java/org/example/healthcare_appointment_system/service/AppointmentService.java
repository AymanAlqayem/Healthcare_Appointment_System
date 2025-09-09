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
import org.example.healthcare_appointment_system.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AvailabilitySlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    public List<AppointmentResponseDto> getMyAppointments() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Doctor not found for current user"));

        return appointmentRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(a -> {
                    AvailabilitySlot slot = a.getSlot();
                    return new AppointmentResponseDto(
                            a.getId(),
                            a.getDoctor().getUser().getUsername(),
                            a.getPatient().getUser().getUsername(),
                            slot.getDayOfWeek().name(),  // use day of week instead of date
                            slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                            slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                            a.getStatus().name()
                    );
                })
                .toList();
    }

    public AppointmentResponseDto bookAppointment(BookAppointmentDto dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Patient not found for current user"));

        // Check if the slot is already booked
        if (appointmentRepository.existsBySlotIdAndStatus(dto.slotId(), AppointmentStatus.BOOKED)) {
            throw new IllegalStateException("This slot is already booked");
        }

        // Find doctor and slot
        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        AvailabilitySlot slot = slotRepository.findById(dto.slotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.isReserved()) {
            throw new RuntimeException("This slot is already booked");
        }

        // Mark the slot as reserved
        slot.setReserved(true);
        slotRepository.save(slot);

        LocalDate date = LocalDate.now().with(TemporalAdjusters.nextOrSame(
                DayOfWeek.valueOf(slot.getDayOfWeek().name())
        ));

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setAppointmentTime(LocalDateTime.of(date, slot.getStartTime()));
        appointment.setStatus(AppointmentStatus.BOOKED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return new AppointmentResponseDto(
                savedAppointment.getId(),
                savedAppointment.getDoctor().getUser().getUsername(),
                savedAppointment.getPatient().getUser().getUsername(),
                slot.getDayOfWeek().name(),
                slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                savedAppointment.getStatus().name()
        );
    }

    public AppointmentResponseDto markAppointmentCompleted(Long appointmentId) {
        Long currentDoctorId = SecurityUtils.getCurrentUserId();

        Doctor doctor = doctorRepository.findByUserId(currentDoctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You can only complete your own appointments");
        }

        if (!appointment.getSlot().isReserved()) {
            throw new RuntimeException("Cannot complete an appointment that is not reserved");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);

        appointment.getSlot().setReserved(false);

        Appointment saved = appointmentRepository.save(appointment);
        slotRepository.save(appointment.getSlot());

        return new AppointmentResponseDto(
                saved.getId(),
                saved.getDoctor().getUser().getUsername(),
                saved.getPatient().getUser().getUsername(),
                saved.getSlot().getDayOfWeek().name(),
                saved.getSlot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                saved.getSlot().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                saved.getStatus().name()
        );
    }

    public List<AvailabilitySlotResponseDto> getMyAvailableSlots() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Doctor doctor = doctorRepository.findAll().stream()
                .filter(d -> d.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user ID: " + currentUserId));

        List<AvailabilitySlot> slots = availabilitySlotRepository.findAll().stream()
                .filter(s -> s.getDoctor().getId().equals(doctor.getId()) && !s.isReserved())
                .toList();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return slots.stream()
                .map(slot -> new AvailabilitySlotResponseDto(
                        slot.getId(),
                        slot.getDayOfWeek().name(),  // use day of week instead of date
                        slot.getStartTime().format(timeFormatter),
                        slot.getEndTime().format(timeFormatter),
                        slot.isReserved()
                ))
                .toList();
    }
}
