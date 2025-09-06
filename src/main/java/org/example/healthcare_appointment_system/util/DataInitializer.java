package org.example.healthcare_appointment_system.util;

import org.example.healthcare_appointment_system.entity.Role;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            var admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(Role.ADMIN))
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }
}
