package com.imri.spring_security.service;

import com.imri.spring_security.dto.JwtResponse;
import com.imri.spring_security.dto.LoginRequest;
import com.imri.spring_security.dto.RegisterRequest;
import com.imri.spring_security.entity.AppRole;
import com.imri.spring_security.entity.User;
import com.imri.spring_security.enums.Role;
import com.imri.spring_security.repository.RoleRepository;
import com.imri.spring_security.repository.UserRepository;
import com.imri.spring_security.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // ── LOGIN ─────────────────────────────────────────────────────────
    public JwtResponse login(LoginRequest request) {

        // 1. AuthenticationManager verifies username + password
        //    It calls UserDetailsServiceImpl.loadUserByUsername internally
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Store authentication in SecurityContext for this request
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);

        // 4. Extract roles for the response
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 5. Get user from DB to include email in response
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        return new JwtResponse(jwt, user.getUsername(), user.getEmail(), roles);
    }

    // ── REGISTER ──────────────────────────────────────────────────────
    public String register(RegisterRequest request) {

        // 1. Check username not already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        // 2. Check email not already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // 3. Build new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt hash
        user.setEnabled(true);

        // 4. Assign default ROLE_USER to every new registration
        AppRole userRole = roleRepository.findByRoleName(Role.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        user.setRoles(Set.of(userRole));

        // 5. Save to database
        userRepository.save(user);

        return "User registered successfully";
    }
}