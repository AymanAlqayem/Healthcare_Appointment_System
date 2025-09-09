package org.example.healthcare_appointment_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.healthcare_appointment_system.enums.Role;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private boolean enabled = true;  // default active

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}