package com.imri.spring_security.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    // OncePerRequestFilter guarantees this runs exactly
    // once per HTTP request — never twice

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Step 1 — Extract JWT from the Authorization header
            String jwt = extractToken(request);

            // Step 2 — If token exists, validate and set authentication
            if (jwt != null) {
                String username = jwtUtils.getUsernameFromToken(jwt);

                // Step 3 — Load user from DB
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                // Step 4 — Validate token matches this user
                if (jwtUtils.validateToken(jwt, userDetails)) {

                    // Step 5 — Create authentication object
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                        // credentials (not needed after auth)
                                    userDetails.getAuthorities() // roles
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    // Step 6 — Tell Spring Security this request is authenticated
                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Step 7 — Always continue the filter chain
        // whether authenticated or not
        filterChain.doFilter(request, response);
    }

    // Pull the token out of "Authorization: Bearer <token>"
    private String extractToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth)
                && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // remove "Bearer " prefix
        }
        return null;
    }
}