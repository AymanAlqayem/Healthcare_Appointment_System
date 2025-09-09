package org.example.healthcare_appointment_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.AuthRequest;
import org.example.healthcare_appointment_system.dto.AuthResponse;
import org.example.healthcare_appointment_system.dto.RefreshRequest;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.example.healthcare_appointment_system.security.CustomUserDetailsService;
import org.example.healthcare_appointment_system.security.JwtService;
import org.example.healthcare_appointment_system.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate tokens
        String username = authentication.getName();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Extract roles
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .collect(Collectors.toList());

        // Generate JWT access token
        String accessToken = jwtService.generateAccessToken(username, roles);

        // Generate JWT refresh token
        String refreshToken = jwtService.generateRefreshToken(username);

        return ResponseEntity.ok(new AuthResponse(
                accessToken,
                jwtService.getExpirationMs(),
                refreshToken
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // Try to get the currently logged-in user ID
            Long userId = SecurityUtils.getCurrentUserId();

            // Stateless logout: client should discard tokens
            return ResponseEntity.noContent().build(); // 204 No Content

        } catch (RuntimeException e) {
            // User not authenticated
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "You are already not logged in"));
        }
    }



    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. Validate refresh token (signature, expiration, type)
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // 2. Extract username
        String username = jwtService.extractUsername(refreshToken);

        // 3. Load user details for roles
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .toList();

        // 4. Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(username, roles);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        // 5. Return response
        return ResponseEntity.ok(new AuthResponse(
                newAccessToken,
                jwtService.getExpirationMs(),
                newRefreshToken
        ));
    }


}
