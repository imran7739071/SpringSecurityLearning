package com.imri.spring_security.service;

import com.imri.spring_security.dto.RoleAssignRequest;
import com.imri.spring_security.dto.UserResponse;
import com.imri.spring_security.entity.AppRole;
import com.imri.spring_security.entity.User;
import com.imri.spring_security.enums.Role;
import com.imri.spring_security.exception.ResourceNotFoundException;
import com.imri.spring_security.repository.RoleRepository;
import com.imri.spring_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // ── GET ALL USERS ─────────────────────────────────────────────────
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── GET USER BY ID ────────────────────────────────────────────────
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        return mapToResponse(user);
    }

    // ── ASSIGN ROLE ───────────────────────────────────────────────────
    public UserResponse assignRole(Long userId, RoleAssignRequest request) {
        log.info("Assigning role: '{}' to user id: {}", request.getRoleName(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userId));

        Role roleEnum;
        try {
            roleEnum = Role.valueOf(request.getRoleName().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role name: " + request.getRoleName());
        }

        AppRole role = roleRepository.findByRoleName(roleEnum)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + roleEnum));

        Set<AppRole> newRoles = new HashSet<>();
        newRoles.add(role);
        user.setRoles(newRoles);

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    // ── TOGGLE ENABLE / DISABLE ───────────────────────────────────────
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        user.setEnabled(!user.isEnabled());   // flip true→false or false→true
        userRepository.save(user);

        return mapToResponse(user);
    }

    // ── DELETE USER ───────────────────────────────────────────────────
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // ── MAPPER ────────────────────────────────────────────────────────
    private UserResponse mapToResponse(User user) {
        Set<String> roleNames = user.getRoles()
                .stream()
                .map(r -> r.getRoleName().name())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .oauthProvider(user.getOauthProvider())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}