package org.example.healthcare_appointment_system.controller;

import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.AuthRequest;
import org.example.healthcare_appointment_system.dto.AuthResponse;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.example.healthcare_appointment_system.security.JwtService;
import org.example.healthcare_appointment_system.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        var roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .toList();

        String accessToken = jwtService.generateAccessToken(request.username(), roles);
        String refreshTokenStr = jwtService.generateRefreshTokenString();

        var user = userRepository.findByUsername(request.username()).orElseThrow();
        refreshTokenService.createRefreshToken(user, refreshTokenStr);

        return ResponseEntity.ok(new AuthResponse(accessToken, jwtService.getExpirationMs(), refreshTokenStr));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null) throw new IllegalArgumentException("refreshToken required");

        var opt = refreshTokenService.findByToken(refreshToken);
        if (opt.isEmpty()) throw new IllegalArgumentException("Invalid refresh token");

        var rt = opt.get();
        if (refreshTokenService.isExpired(rt)) {
            refreshTokenService.deleteByToken(refreshToken);
            throw new IllegalArgumentException("Refresh token expired");
        }

        String username = rt.getUser().getUsername();
        var userRoles = List.of(rt.getUser().getRole().name());
        String newAccess = jwtService.generateAccessToken(username, userRoles);
        return ResponseEntity.ok(new AuthResponse(newAccess, jwtService.getExpirationMs(), refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken != null) refreshTokenService.deleteByToken(refreshToken);
        return ResponseEntity.noContent().build();
    }
}
