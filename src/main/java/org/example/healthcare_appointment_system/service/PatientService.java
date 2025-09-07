package org.example.healthcare_appointment_system.service;

//import org.example.healthcare_appointment_system.dto.PatientDto;
//import org.example.healthcare_appointment_system.entity.Patient;
//import org.example.healthcare_appointment_system.repo.PatientRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//public class PatientService {
//    private final PatientRepository patientRepository;
//
//    public PatientService(PatientRepository patientRepository) { this.patientRepository = patientRepository; }
//
//    public List<PatientDto> list() {
//        return patientRepository.findAll().stream().map(this::toDto).toList();
//    }
//
//    public PatientDto get(Long id) {
//        return patientRepository.findById(id).map(this::toDto)
//                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
//    }
//
//    @Transactional
//    public PatientDto create(PatientDto dto) {
//        var p = Patient.builder()
//                .firstName(dto.firstName())
//                .lastName(dto.lastName())
//                .phone(dto.phone())
//                .email(dto.email())
//                .build();
//        return toDto(patientRepository.save(p));
//    }
//
//    @Transactional
//    public PatientDto update(Long id, PatientDto dto) {
//        var p = patientRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Patient not found"));
//        p.setFirstName(dto.firstName());
//        p.setLastName(dto.lastName());
//        p.setPhone(dto.phone());
//        p.setEmail(dto.email());
//        return toDto(patientRepository.save(p));
//    }
//
//    @Transactional
//    public void delete(Long id) { patientRepository.deleteById(id); }
//
//    private PatientDto toDto(Patient p) {
//        return new PatientDto(p.getId(), p.getFirstName(), p.getLastName(), p.getPhone(), p.getEmail());
//    }
//}

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.PatientDto;
import org.example.healthcare_appointment_system.dto.PatientResponseDto;
import org.example.healthcare_appointment_system.entity.Patient;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.Gender;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.PatientRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

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

}

