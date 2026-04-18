package com.imri.spring_security.config;
import com.imri.spring_security.entity.AppRole;
import com.imri.spring_security.entity.User;
import com.imri.spring_security.enums.Role;
import com.imri.spring_security.repository.RoleRepository;
import com.imri.spring_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor   // Lombok: generates constructor for all final fields
@Slf4j                     // Lombok: gives us log.info(), log.error() etc.
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedRoles();
        seedSuperAdmin();
    }

    // ── Create all 4 roles if they don't already exist ──────────────
    private void seedRoles() {
        for (Role role : Role.values()) {
            roleRepository.findByRoleName(role).orElseGet(() -> {
                AppRole newRole = new AppRole();
                newRole.setRoleName(role);
                AppRole saved = roleRepository.save(newRole);
                log.info("Created role: {}", role.name());
                return saved;
            });
        }
    }

    // ── Create first superadmin account if it doesn't exist ─────────
    private void seedSuperAdmin() {
        if (userRepository.existsByUsername("superadmin")) {
            log.info("Superadmin already exists, skipping.");
            return;
        }

        AppRole superAdminRole = roleRepository
                .findByRoleName(Role.ROLE_SUPER_ADMIN)
                .orElseThrow(() -> new RuntimeException("ROLE_SUPER_ADMIN not found"));

        User superAdmin = new User();
        superAdmin.setUsername("superadmin");
        superAdmin.setEmail("superadmin@app.com");
        superAdmin.setPassword(passwordEncoder.encode("Admin@1234"));
        superAdmin.setEnabled(true);
        //superAdmin.setRoles(Set.of(superAdminRole));
        Set<AppRole> roles = new HashSet<>();
        roles.add(superAdminRole);
        superAdmin.setRoles(roles);

        userRepository.save(superAdmin);
        log.info("Superadmin user created successfully.");
    }
}
