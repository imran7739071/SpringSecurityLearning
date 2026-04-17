package com.imri.spring_security.repository;
import com.imri.spring_security.entity.AppRole;
import com.imri.spring_security.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<AppRole, Long> {

    // Spring Data auto-generates:
    // SELECT * FROM roles WHERE role_name = ?
    Optional<AppRole> findByRoleName(Role roleName);
}