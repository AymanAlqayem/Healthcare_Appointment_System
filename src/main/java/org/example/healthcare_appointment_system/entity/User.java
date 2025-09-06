package org.example.healthcare_appointment_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    //tells JPA this is not another entity,
    // but a collection of basic values (in this case, enums).
    @ElementCollection(fetch = FetchType.EAGER)
    //store the enum’s name (e.g., "ADMIN") instead of its ordinal (like 0).
    @Enumerated(EnumType.STRING)

    // Creates a separate table user_roles.
    //Maps each role to the user_id.
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    private boolean enabled = true;
}