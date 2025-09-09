package org.example.healthcare_appointment_system.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Getter
public class JwtService {
    private final Key key;//cryptographic key used to sign and verify JWTs
    private final long expirationMs;
    private final long refreshExpirationMs; // Add refresh token expiration

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expirationMs,
            @Value("${app.jwt.refresh-expiration}") long refreshExpirationMs
    ) {
        //builds an HMAC-SHA256 key from the secret string.
        //This key is used both for signing tokens and validating them.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
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

    public String generateAccessToken(String username, Collection<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("type", "access")  // <-- Add type claim
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")  // <-- Add type claim
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


//    public String generateAccessToken(String username, Collection<String> roles) {
//        Date now = new Date();
//        Date exp = new Date(now.getTime() + expirationMs);
//        return Jwts.builder()
//                .setSubject(username)
//                .claim("roles", roles)
//                .setIssuedAt(now)
//                .setExpiration(exp)
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//
//    public String generateRefreshToken(String username) {
//        Date now = new Date();
//        Date exp = new Date(now.getTime() + refreshExpirationMs);
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(now)
//                .setExpiration(exp)
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }

    public boolean isRefreshTokenValid(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            // Check expiration
            boolean notExpired = !claims.getBody().getExpiration().before(new Date());
            // Check type
            boolean isRefresh = "refresh".equals(claims.getBody().get("type", String.class));

            return notExpired && isRefresh;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

//    public boolean isRefreshTokenValid(String token) {
//        try {
//            Jws<Claims> claims = Jwts.parser()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token);
//            return !claims.getBody().getExpiration().before(new Date());
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
}
