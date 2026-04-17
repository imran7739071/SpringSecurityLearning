package com.imri.spring_security.entity;

import com.imri.spring_security.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)   // stores "ROLE_ADMIN" not 0,1,2
    @Column(unique = true, nullable = false)
    private Role roleName;
}
