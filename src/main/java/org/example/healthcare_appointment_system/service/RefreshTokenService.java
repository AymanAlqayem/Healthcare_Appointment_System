package org.example.healthcare_appointment_system.service;

import org.example.healthcare_appointment_system.entity.RefreshToken;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.repo.RefreshTokenRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               @Value("${app.jwt.refreshExpiration}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public RefreshToken createRefreshToken(User user, String tokenString) {
        var expiry = Instant.now().plusMillis(refreshExpirationMs);
        RefreshToken rt = RefreshToken.builder()
                .token(tokenString)
                .expiryDate(expiry)
                .user(user)
                .build();
        return refreshTokenRepository.save(rt);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }


    public boolean isExpired(RefreshToken rt) {
        return rt.getExpiryDate().isBefore(Instant.now());
    }

    @Transactional
    public int deleteByUser(User user) {
        return refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}