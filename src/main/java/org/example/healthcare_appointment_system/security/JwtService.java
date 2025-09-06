package org.example.healthcare_appointment_system.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {
    private final Key key;//cryptographic key used to sign and verify JWTs
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expirationMs
    ) {
        //builds an HMAC-SHA256 key from the secret string.
        //This key is used both for signing tokens and validating them.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }


    public String extractUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    /**
     * Gets the "roles" claim.
     * Converts it to a list of strings (like ["ADMIN", "DOCTOR"]).
     * Useful for building GrantedAuthority objects in Spring Security.
     * */
    public List<String> extractRoles(String token) {
        Object roles = parse(token).getBody().get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().setSigningKey(key)// verify with the same key
                .build()
                .parseClaimsJws(token);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public String generateAccessToken(String username, Collection<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshTokenString() {
        // A refresh token can be a JWT or a random UUID. We'll use a cryptographically strong UUID here.
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
}
