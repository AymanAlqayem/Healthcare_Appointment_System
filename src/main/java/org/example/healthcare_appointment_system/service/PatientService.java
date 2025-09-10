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
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.*;
import org.example.healthcare_appointment_system.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;

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

        return new PatientResponseDto(
                savedPatient.getId(),
                savedPatient.getUser().getUsername(),
                savedPatient.getUser().getEmail(),
                savedPatient.getGender().name(),
                savedPatient.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        );
    }

    public ResponseEntity<String> deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        User user = patient.getUser();

        patientRepository.delete(patient);

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
                        patient.getGender().name(),
                        patient.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                ))
                .toList();
    }

    public PatientResponseDto updateInfo(PatientUpdateDto dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Patient not found for current user"));

        patient.getUser().setPhone(dto.phone());
        patient.getUser().setEmail(dto.email());
        patient.setGender(dto.gender());
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
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Patient not found for current user"));

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

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        AvailabilitySlot slot = appointment.getSlot();

        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getDoctor().getUser().getUsername(),
                appointment.getPatient().getUser().getUsername(),
                slot.getDayOfWeek().name(),
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

    public PatientMedicalHistoryDto getPatientHistory() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Patient patient = patientRepository.findAll().stream()
                .filter(p -> p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Patient profile not found for user ID: " + currentUserId));

        Long patientId = patient.getId();
        String patientName = patient.getUser().getUsername();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<PrescriptionHistoryDto> prescriptions = prescriptionRepository.findByPatientId(patientId)
                .stream()
                .map(p -> {
                    Appointment appointment = appointmentRepository.findById(p.getAppointmentId())
                            .orElseThrow(() -> new RuntimeException("Appointment not found: " + p.getAppointmentId()));

                    String day = appointment.getSlot().getDayOfWeek().name();
                    String time = appointment.getSlot().getStartTime().format(timeFormatter) +
                            " - " + appointment.getSlot().getEndTime().format(timeFormatter);

                    String createdAt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                            .withZone(ZoneId.systemDefault())
                            .format(p.getCreatedAt());

                    return new PrescriptionHistoryDto(day, time, createdAt, p.getNotes(), p.getMedicines());
                })
                .toList();

        List<MedicalRecordHistoryDto> medicalRecords = medicalRecordRepository.findByPatientId(patientId)
                .stream()
                .map(r -> new MedicalRecordHistoryDto(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                                .withZone(ZoneId.systemDefault())
                                .format(r.getCreatedAt()),
                        r.getNotes()
                ))
                .toList();

        return new PatientMedicalHistoryDto(patientId, patientName, prescriptions, medicalRecords);
    }
}

