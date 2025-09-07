package org.example.healthcare_appointment_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.AdminDto;
import org.example.healthcare_appointment_system.dto.AdminResponseDto;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminResponseDto createAdmin(AdminDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhone(dto.phone())) {
            throw new RuntimeException("Phone already exists");
        }

        // Create admin user
        User admin = User.builder()
                .username(dto.username())
                .email(dto.email())
                .phone(dto.phone())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        // Save to DB
        User savedAdmin = userRepository.save(admin);

        // Build response DTO manually
        return new AdminResponseDto(
                savedAdmin.getId(),
                savedAdmin.getUsername(),
                savedAdmin.getEmail(),
                savedAdmin.getPhone()
        );
    }


}

