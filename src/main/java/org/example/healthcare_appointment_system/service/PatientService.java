package org.example.healthcare_appointment_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.AOP.CancelAppointmentCheck;
import org.example.healthcare_appointment_system.dto.*;
import org.example.healthcare_appointment_system.entity.Appointment;
import org.example.healthcare_appointment_system.entity.AvailabilitySlot;
import org.example.healthcare_appointment_system.entity.Patient;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.AppointmentStatus;
import org.example.healthcare_appointment_system.enums.Gender;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.AppointmentRepository;
import org.example.healthcare_appointment_system.repo.PatientRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.example.healthcare_appointment_system.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public PatientResponseDto createPatient(PatientDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhone(dto.phone())) {
            throw new RuntimeException("Phone already exists");
        }

        // Create User account
        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .phone(dto.phone())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.PATIENT)
                .enabled(true)
                .build();

        userRepository.save(user);

        // Create Patient profile linked to user
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setGender(dto.gender());  // directly assign
        patient.setDateOfBirth(dto.dateOfBirth());

        Patient savedPatient = patientRepository.save(patient);

        // Convert to response DTO
        return new PatientResponseDto(
                savedPatient.getId(),
                savedPatient.getUser().getUsername(),
                savedPatient.getUser().getEmail(),
                savedPatient.getGender().name(),  // convert enum to String
                savedPatient.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        );
    }

    public ResponseEntity<String> deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        //Also delete the associated User if desired
        User user = patient.getUser();

        // Delete doctor entity
        patientRepository.delete(patient);

        // Delete the associated user
        if (user != null) {
            userRepository.delete(user);
        }
        return ResponseEntity.ok("Patient deleted successfully");

    }

    public List<PatientResponseDto> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(patient -> new PatientResponseDto(
                        patient.getId(),
                        patient.getUser().getUsername(),
                        patient.getUser().getEmail(),
                        patient.getGender().name(), // convert enum to string
                        patient.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                ))
                .toList();
    }

    public PatientResponseDto updateInfo(PatientUpdateDto dto) {
        // Get the currently logged-in user's ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // Find the patient associated with this user ID
        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Patient not found for current user"));

        // Update fields - no need to find by ID since we already have the patient
        patient.getUser().setPhone(dto.phone());
        patient.getUser().setEmail(dto.email());
        patient.setGender(dto.gender());  // Convert string to enum if needed
        patient.setDateOfBirth(dto.dateOfBirth());

        Patient savedPatient = patientRepository.save(patient);

        return new PatientResponseDto(
                savedPatient.getId(),
                savedPatient.getUser().getUsername(),
                savedPatient.getUser().getEmail(),
                savedPatient.getGender().name(),
                savedPatient.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        );
    }

    public List<AppointmentResponseDto> getMyAppointments() {
        // 1. Get the currently logged-in user's ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 2. Find the patient associated with this user ID
        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Patient not found for current user"));

        // 3. Get appointments for this specific patient
        return appointmentRepository.findByPatientId(patient.getId())
                .stream()
                .map(app -> {
                    AvailabilitySlot slot = app.getSlot();

                    return new AppointmentResponseDto(
                            app.getId(),
                            app.getDoctor().getUser().getUsername(),
                            app.getPatient().getUser().getUsername(),
                            slot.getDayOfWeek().name(),                     // day of week instead of date
                            slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                            slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                            app.getStatus().name()
                    );
                })
                .toList();
    }

    @CancelAppointmentCheck
    @Transactional
    public AppointmentResponseDto cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Update status
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        AvailabilitySlot slot = appointment.getSlot();

        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getDoctor().getUser().getUsername(),
                appointment.getPatient().getUser().getUsername(),
                slot.getDayOfWeek().name(),  // use day of week instead of date
                slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                appointment.getStatus().name()
        );
    }

    public ResponseEntity<PatientResponseDto> findPatient(String name) {
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + name));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No patient profile found for user: " + name));

        PatientResponseDto dto = new PatientResponseDto(
                patient.getId(),
                user.getUsername(),
                user.getEmail(),
                patient.getGender().name(),
                patient.getDateOfBirth().toString()
        );

        return ResponseEntity.ok(dto);
    }


}

