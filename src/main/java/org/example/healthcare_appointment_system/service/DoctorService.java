package org.example.healthcare_appointment_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.DoctorDto;
import org.example.healthcare_appointment_system.dto.DoctorResponseDto;
import org.example.healthcare_appointment_system.entity.Doctor;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.DoctorRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DoctorResponseDto createDoctor(DoctorDto dto) {
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
                .role(Role.DOCTOR)
                .enabled(true)
                .build();

        userRepository.save(user);

        // Create Doctor profile linked to user
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialty(dto.specialty());

        // Save doctor to DB
        Doctor savedDoctor = doctorRepository.save(doctor);

        // Return response DTO
        return new DoctorResponseDto(
                savedDoctor.getId(),
                savedDoctor.getUser().getUsername(),
                savedDoctor.getUser().getEmail(),
                savedDoctor.getUser().getPhone(),
                savedDoctor.getSpecialty()
        );
    }

    public List<DoctorResponseDto> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(doctor -> new DoctorResponseDto(
                        doctor.getId(),
                        doctor.getUser().getUsername(),
                        doctor.getUser().getEmail(),
                        doctor.getUser().getPhone(),
                        doctor.getSpecialty()
                ))
                .toList();
    }
}



