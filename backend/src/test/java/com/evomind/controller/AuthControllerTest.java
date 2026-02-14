package com.evomind.controller;

import com.evomind.dto.request.LoginRequest;
import com.evomind.dto.request.RegisterRequest;
import com.evomind.dto.response.AuthResponse;
import com.evomind.service.UserService;
import com.evomind.service.VerificationCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private VerificationCodeService verificationCodeService;

    @BeforeEach
    void setUp() {
        // Setup test data
    }

    @Test
    void testLoginWithValidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setPhone("13800138000");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse();
        response.setAccessToken("test-token");
        response.setTokenType("Bearer");

        when(userService.login(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("test-token"));
    }

    @Test
    void testRegisterWithValidData() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setPassword("password123");
        request.setVerificationCode("123456");

        when(verificationCodeService.verifyCode("13800138000", "123456")).thenReturn(true);
        when(userService.register(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testLoginWithInvalidPhone() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setPhone("invalid");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
