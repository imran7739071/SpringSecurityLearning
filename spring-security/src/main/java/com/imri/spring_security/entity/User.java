package com.imri.spring_security.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = true)        // nullable because OAuth2 users have no password
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = true)
    private String oauthProvider;   // "google", "github" or null for normal login

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",                              // junction table name
            joinColumns = @JoinColumn(name = "user_id"),      // FK to users table
            inverseJoinColumns = @JoinColumn(name = "role_id") // FK to roles table
    )
    private Set<AppRole> roles = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}