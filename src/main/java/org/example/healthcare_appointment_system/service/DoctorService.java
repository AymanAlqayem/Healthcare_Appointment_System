package org.example.healthcare_appointment_system.service;

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

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public Doctor createDoctor(DoctorDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email already exists");
        }

        // Create User account
        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.DOCTOR)
                .enabled(true)
                .build();
        userRepository.save(user);

        // Create Doctor profile linked to user
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialty(dto.specialty());

        return doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
}



