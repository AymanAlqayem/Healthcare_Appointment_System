package org.example.healthcare_appointment_system.service;

import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.RegisterRequest;
import org.example.healthcare_appointment_system.dto.UserDto;
import org.example.healthcare_appointment_system.dto.UserResponseDto;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

//@Service
//public class UserService {
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Transactional
//    public UserDto register(RegisterRequest req) {
//        // Check if username already exists
//        if (userRepository.existsByUsername(req.username())) {
//            throw new IllegalArgumentException("Username already taken");
//        }
//
//        // Roles are already Set<Role> in DTO, no need to convert
//        Set<Role> roles = req.roles() != null && !req.roles().isEmpty()
//                ? req.roles()
//                : Set.of(Role.PATIENT); // default role if none provided
//
//        // Build User entity
//        User user = User.builder()
//                .username(req.username())
//                .password(passwordEncoder.encode(req.password()))
//                .email(req.email())
//                .phone(req.phone())
//                .roles(roles)
//                .enabled(true) // important for Spring Security
//                .build();
//
//        User saved = userRepository.save(user);
//
//        return new UserDto(
//                saved.getId(),
//                saved.getUsername(),
//                saved.getEmail(),
//                saved.getPhone(),
//                saved.getRoles()
//        );
//
//    }
//}

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto createUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .roles(dto.roles())
                .enabled(true)
                .build();

        userRepository.save(user);

        return new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getRoles());
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}


