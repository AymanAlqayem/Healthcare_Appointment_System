package org.example.healthcare_appointment_system.service;

import org.example.healthcare_appointment_system.dto.RegisterRequest;
import org.example.healthcare_appointment_system.dto.UserDto;
import org.example.healthcare_appointment_system.entity.Role;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username already taken");
        }
        //Convert role strings to Role enum
        Set<Role> roles = req.roles().stream()
                .map(r -> Role.valueOf(r.toUpperCase()))
                .collect(java.util.stream.Collectors.toSet());

        var user = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .roles(roles)
                .enabled(true)
                .build();

        var saved = userRepository.save(user);

        return new UserDto(saved.getId(),
                saved.getUsername(),
                saved.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
    }
}