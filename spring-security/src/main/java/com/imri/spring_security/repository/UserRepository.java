package com.imri.spring_security.repository;

import com.imri.spring_security.entity.User;
import com.imri.spring_security.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // Custom JPQL — finds all users who have a specific role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :role")
    List<User> findAllByRole(@Param("role") Role role);
}
