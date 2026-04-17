package com.imri.spring_security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imri.spring_security.dto.JwtResponse;
import com.imri.spring_security.dto.LoginRequest;
import com.imri.spring_security.dto.RegisterRequest;
import com.imri.spring_security.security.JwtAuthFilter;
import com.imri.spring_security.security.OAuth2LoginSuccessHandler;
import com.imri.spring_security.security.UserDetailsServiceImpl;
import com.imri.spring_security.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    void login_shouldReturnJwtResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("imran");
        request.setPassword("secret");

        JwtResponse response = new JwtResponse(
                "token-value",
                "imran",
                "imran@example.com",
                List.of("ROLE_ADMIN")
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-value"))
                .andExpect(jsonPath("$.username").value("imran"))
                .andExpect(jsonPath("$.email").value("imran@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void register_shouldReturnSuccessMessage() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new-user");
        request.setEmail("new-user@example.com");
        request.setPassword("secret");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn("User registered successfully");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("User registered successfully"));
    }
}
