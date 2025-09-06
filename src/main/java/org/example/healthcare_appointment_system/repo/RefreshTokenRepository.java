package org.example.healthcare_appointment_system.repo;


import org.example.healthcare_appointment_system.entity.RefreshToken;
import org.example.healthcare_appointment_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    int deleteByUser(User user);
}
