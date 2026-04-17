package com.imri.spring_security.security;

import com.imri.spring_security.entity.AppRole;
import com.imri.spring_security.entity.User;
import com.imri.spring_security.enums.Role;
import com.imri.spring_security.repository.RoleRepository;
import com.imri.spring_security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 1. Get the Google user info that came back from Google
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // 2. Extract email and name from Google's response
        String email = oauth2User.getAttribute("email");
        String name  = oauth2User.getAttribute("name");

        log.info("OAuth2 login success for email: {}", email);

        // 3. Find existing user OR create a new one
        //    This handles both first-time Google login and returning users
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewOAuth2User(email, name));

        // 4. Load as UserDetails so JwtUtils can build the token
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getUsername());

        // 5. Generate our own JWT — same format as normal login
        String jwt = jwtUtils.generateToken(userDetails);

        // 6. Redirect to Angular with token in URL query param
        //    Angular's OAuth2CallbackComponent will extract and store it
        String redirectUrl = "http://localhost:4200/oauth2/callback?token=" + jwt;
        response.sendRedirect(redirectUrl);
    }

    // Called only when this is a brand-new Google user (first time login)
    private User createNewOAuth2User(String email, String name) {
        log.info("Creating new OAuth2 user for: {}", email);

        // Build a username from the part before @ in email
        // e.g. "john.doe@gmail.com" → "john.doe"
        String username = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9]", ""); // remove special chars

        // If that username is already taken, append a number
        String finalUsername = username;
        int counter = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = username + counter++;
        }

        // Assign the default ROLE_USER to all OAuth2 users
        AppRole userRole = roleRepository
                .findByRoleName(Role.ROLE_USER)
                .orElseThrow(() -> new RuntimeException(
                        "ROLE_USER not found in database"));

        User newUser = new User();
        newUser.setUsername(finalUsername);
        newUser.setEmail(email);
        newUser.setPassword(null);           // no password for OAuth2 users
        newUser.setOauthProvider("google");  // mark where they came from
        newUser.setEnabled(true);
        newUser.setRoles(Set.of(userRole));

        return userRepository.save(newUser);
    }
}