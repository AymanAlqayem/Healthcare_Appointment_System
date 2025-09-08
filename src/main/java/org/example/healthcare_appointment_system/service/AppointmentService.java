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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AvailabilitySlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public List<AppointmentResponseDto> getMyAppointments() {
        // Get the currently logged-in user's ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // Find the doctor associated with this user ID
        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Doctor not found for current user"));

        // Get appointments for this specific doctor
        return appointmentRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(a -> new AppointmentResponseDto(
                        a.getId(),
                        a.getDoctor().getUser().getUsername(),
                        a.getPatient().getUser().getUsername(),
                        a.getSlot().getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        a.getSlot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        a.getSlot().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        a.getStatus().name()
                ))
                .toList();
    }

    public AppointmentResponseDto bookAppointment(BookAppointmentDto dto) {
        // Get the currently logged-in user's ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // Find the patient associated with this user ID
        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Patient not found for current user"));

        if (appointmentRepository.existsBySlotIdAndStatus(dto.slotId(), AppointmentStatus.BOOKED)) {
            throw new IllegalStateException("This slot is already booked");
        }

        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

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
        appointment.setPatient(patient);  // Use the auto-detected patient
        appointment.setSlot(slot);
        appointment.setAppointmentTime(LocalDateTime.of(slot.getDate(), slot.getStartTime()));
        appointment.setStatus(AppointmentStatus.BOOKED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

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

    public AppointmentResponseDto markAppointmentCompleted(Long appointmentId) {
        // Get the currently logged-in doctor's ID
        Long currentDoctorId = SecurityUtils.getCurrentUserId();

        // First, get the doctor entity to ensure it exists
        Doctor doctor = doctorRepository.findById(currentDoctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Ensure the appointment belongs to this doctor
        if (!appointment.getDoctor().getId().equals(currentDoctorId)) {
            throw new RuntimeException("You can only complete your own appointments");
        }

        // Ensure the appointment is reserved
        if (!appointment.getSlot().isReserved()) {
            throw new RuntimeException("Cannot complete an appointment that is not reserved");
        }

        // Update status
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);

        return new AppointmentResponseDto(
                saved.getId(),
                saved.getDoctor().getUser().getUsername(),
                saved.getPatient().getUser().getUsername(),
                saved.getSlot().getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                saved.getSlot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                saved.getSlot().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                saved.getStatus().name()
        );
    }

    public List<AvailabilitySlotResponseDto> getMyAvailableSlots() {
        // Get the currently logged-in user's ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // Find the doctor associated with this user ID
        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Doctor not found for current user"));

        // Get available (not reserved) slots for this doctor
        List<AvailabilitySlot> availableSlots = slotRepository.findByDoctorIdAndReservedFalse(doctor.getId());

        // Filter to show only future slots (today and beyond)
        List<AvailabilitySlot> futureSlots = availableSlots.stream()
                .filter(slot -> isSlotInFuture(slot))
                .collect(Collectors.toList());

        // Convert to DTO
        return futureSlots.stream()
                .map(slot -> new AvailabilitySlotResponseDto(
                        slot.getId(),
                        slot.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        slot.isReserved()  // This will always be 'false' due to repository filter
                ))
                .toList();
    }

    private boolean isSlotInFuture(AvailabilitySlot slot) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (slot.getDate().isAfter(today)) {
            return true; // Future date
        } else if (slot.getDate().isEqual(today)) {
            return slot.getStartTime().isAfter(now); // Today but future time
        }
        return false; // Past date
    }

}
