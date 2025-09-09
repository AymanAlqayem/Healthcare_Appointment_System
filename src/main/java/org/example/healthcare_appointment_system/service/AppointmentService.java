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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AvailabilitySlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

//    public List<AppointmentResponseDto> getMyAppointments() {
//        // Get the currently logged-in user's ID
//        Long currentUserId = SecurityUtils.getCurrentUserId();
//
//        // Find the doctor associated with this user ID
//        Doctor doctor = doctorRepository.findByUserId(currentUserId)
//                .orElseThrow(() -> new RuntimeException("Doctor not found for current user"));
//
//        // Get appointments for this specific doctor
//        return appointmentRepository.findByDoctorId(doctor.getId())
//                .stream()
//                .map(a -> new AppointmentResponseDto(
//                        a.getId(),
//                        a.getDoctor().getUser().getUsername(),
//                        a.getPatient().getUser().getUsername(),
//                        a.getSlot().getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
//                        a.getSlot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
//                        a.getSlot().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
//                        a.getStatus().name()
//                ))
//                .toList();
//    }

    public List<AppointmentResponseDto> getMyAppointments() {
        // 1. Get the currently logged-in user's ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 2. Find the doctor associated with this user ID
        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Doctor not found for current user"));

        // 3. Get appointments for this doctor
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


//    public AppointmentResponseDto bookAppointment(BookAppointmentDto dto) {
//        // Get the currently logged-in user's ID
//        Long currentUserId = SecurityUtils.getCurrentUserId();
//
//        // Find the patient associated with this user ID
//        Patient patient = patientRepository.findByUserId(currentUserId)
//                .orElseThrow(() -> new RuntimeException("Patient not found for current user"));
//
//        if (appointmentRepository.existsBySlotIdAndStatus(dto.slotId(), AppointmentStatus.BOOKED)) {
//            throw new IllegalStateException("This slot is already booked");
//        }
//
//        Doctor doctor = doctorRepository.findById(dto.doctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found"));
//
//        AvailabilitySlot slot = slotRepository.findById(dto.slotId())
//                .orElseThrow(() -> new RuntimeException("Slot not found"));
//
//        // Prevent double booking
//        if (slot.isReserved()) {
//            throw new RuntimeException("This slot is already booked");
//        }
//
//        // Mark the slot as reserved
//        slot.setReserved(true);
//        slotRepository.save(slot);
//
//        Appointment appointment = new Appointment();
//        appointment.setDoctor(doctor);
//        appointment.setPatient(patient);  // Use the auto-detected patient
//        appointment.setSlot(slot);
//        appointment.setAppointmentTime(LocalDateTime.of(slot.getDate(), slot.getStartTime()));
//        appointment.setStatus(AppointmentStatus.BOOKED);
//
//        Appointment savedAppointment = appointmentRepository.save(appointment);
//
//        return new AppointmentResponseDto(
//                savedAppointment.getId(),
//                savedAppointment.getDoctor().getUser().getUsername(),
//                savedAppointment.getPatient().getUser().getUsername(),
//                savedAppointment.getSlot().getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
//                savedAppointment.getSlot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
//                savedAppointment.getSlot().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
//                savedAppointment.getStatus().name()
//        );
//    }

    public AppointmentResponseDto bookAppointment(BookAppointmentDto dto) {
        // 1. Get the currently logged-in user's ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 2. Find the patient associated with this user ID
        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Patient not found for current user"));

        // 3. Check if the slot is already booked
        if (appointmentRepository.existsBySlotIdAndStatus(dto.slotId(), AppointmentStatus.BOOKED)) {
            throw new IllegalStateException("This slot is already booked");
        }

        // 4. Find doctor and slot
        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        AvailabilitySlot slot = slotRepository.findById(dto.slotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.isReserved()) {
            throw new RuntimeException("This slot is already booked");
        }

        // 5. Mark the slot as reserved
        slot.setReserved(true);
        slotRepository.save(slot);

        // 6. Compute the next date for the slot's dayOfWeek
        LocalDate nextDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(
                DayOfWeek.valueOf(slot.getDayOfWeek().name())
        ));

        // 7. Create appointment
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setAppointmentTime(LocalDateTime.of(nextDate, slot.getStartTime()));
        appointment.setStatus(AppointmentStatus.BOOKED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return new AppointmentResponseDto(
                savedAppointment.getId(),
                savedAppointment.getDoctor().getUser().getUsername(),
                savedAppointment.getPatient().getUser().getUsername(),
                slot.getDayOfWeek().name(),  // return day of week instead of date
                slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                savedAppointment.getStatus().name()
        );
    }


    public AppointmentResponseDto markAppointmentCompleted(Long appointmentId) {
        // Get the currently logged-in doctor's ID
        Long currentDoctorId = SecurityUtils.getCurrentUserId();

        Doctor doctor = doctorRepository.findByUserId(currentDoctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Ensure the appointment belongs to this doctor
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You can only complete your own appointments");
        }

        // Ensure it's actually booked
        if (!appointment.getSlot().isReserved()) {
            throw new RuntimeException("Cannot complete an appointment that is not reserved");
        }

        // Update appointment status
        appointment.setStatus(AppointmentStatus.COMPLETED);

        // Free up the slot again for future reservations
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
        // 1. Get logged-in user ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 2. Find the doctor linked to this user
        Doctor doctor = doctorRepository.findAll().stream()
                .filter(d -> d.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user ID: " + currentUserId));

        // 3. Get slots for this doctor and filter only available (reserved = false)
        List<AvailabilitySlot> slots = availabilitySlotRepository.findAll().stream()
                .filter(s -> s.getDoctor().getId().equals(doctor.getId()) && !s.isReserved())
                .toList();

        // 4. Map to DTO with formatting
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
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


//    public List<AvailabilitySlotResponseDto> getMyAvailableSlots() {
//        // 1. Get logged-in user ID
//        Long currentUserId = SecurityUtils.getCurrentUserId();
//
//        // 2. Find the doctor linked to this user
//        Doctor doctor = doctorRepository.findAll().stream()
//                .filter(d -> d.getUser().getId().equals(currentUserId))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user ID: " + currentUserId));
//
//        // 3. Get slots for this doctor
//        List<AvailabilitySlot> slots = availabilitySlotRepository.findAll().stream()
//                .filter(s -> s.getDoctor().getId().equals(doctor.getId()))
//                .toList();
//
//        // 4. Map to DTO with formatting
//        return slots.stream()
//                .map(slot -> new AvailabilitySlotResponseDto(
//                        slot.getId(),
//                        slot.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
//                        slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
//                        slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
//                        slot.isReserved()
//                ))
//                .toList();
//    }


//    private boolean isSlotInFuture(AvailabilitySlot slot) {
//        LocalDate today = LocalDate.now();
//        LocalTime now = LocalTime.now();
//
//        if (slot.getDate().isAfter(today)) {
//            return true; // Future date
//        } else if (slot.getDate().isEqual(today)) {
//            return slot.getStartTime().isAfter(now); // Today but future time
//        }
//        return false; // Past date
//    }

}
