package org.example.healthcare_appointment_system.config;

import org.example.healthcare_appointment_system.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter; //validates JWT tokens

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * filterChain method that will define the security rules for application
     */
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                //we use JWT instead of sessions
//                .csrf(csrf -> csrf.disable())
//                //Spring won’t create or use HTTP sessions. Every request must carry its JWT token.
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> auth
//                        //authorization rules.
//                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/api/auth/**", "/h2-console/**").permitAll()
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/doctor/**").hasAnyRole("DOCTOR", "ADMIN")
//                        .requestMatchers("/api/patient/**").hasAnyRole("PATIENT", "DOCTOR", "ADMIN")
//                        .anyRequest().authenticated() //everything else requires a logged-in user
//                ).headers(h -> h.frameOptions(f -> f.disable())) // allows the H2 console UI
//                // Spring Security will check JWTs first,before falling back to form login.
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html",
                                "/swagger-ui/**", "/api/auth/**", "/h2-console/**").permitAll()

                        // ADMIN endpoints - ONLY ADMIN can access these
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // DOCTOR endpoints - ONLY DOCTOR can access these
                        .requestMatchers("/api/doctor/**").hasRole("DOCTOR")

                        // PATIENT endpoints - ONLY PATIENT can access these
                        .requestMatchers("/api/patient/**").hasRole("PATIENT")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .headers(h -> h.frameOptions(f -> f.disable()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    /**
     * authenticationManager is used by Spring Security to authenticate credentials (like when a user logs in).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * passwordEncoder method that will hash the password.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}