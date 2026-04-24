package com.udea.bancodigital.auth.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessRequestDto;
import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessResponseDto;
import com.udea.bancodigital.auth.domain.port.in.ProvisionClientAccessPort;
import com.udea.bancodigital.auth.infrastructure.config.JwtAuthenticationFilter;
import com.udea.bancodigital.infrastructure.config.SecurityConfig;
import com.udea.bancodigital.shared.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({IdentityController.class, IdentityProvisioningController.class})
@Import(SecurityConfig.class)
class IdentityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProvisionClientAccessPort provisionClientAccessPort;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(
                    invocation.getArgument(0, ServletRequest.class),
                    invocation.getArgument(1, ServletResponse.class)
            );
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void deberiaRetornarIdentidadAutenticada() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        AuthenticatedUser principal = new AuthenticatedUser(userId, "cliente@test.com", clienteId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        mockMvc.perform(get("/api/v1/auth/me").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.clienteId").value(clienteId.toString()))
                .andExpect(jsonPath("$.username").value("cliente@test.com"));
    }

    @Test
    void deberiaProvisionarAccesoDesdeEndpointInterno() throws Exception {
        UUID clienteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ProvisionClientAccessRequestDto request = ProvisionClientAccessRequestDto.builder()
                .clienteId(clienteId)
                .email("cliente@test.com")
                .build();

        when(provisionClientAccessPort.provisionClientAccess(any(ProvisionClientAccessRequestDto.class)))
                .thenReturn(ProvisionClientAccessResponseDto.builder()
                        .userId(userId)
                        .clienteId(clienteId)
                        .email("cliente@test.com")
                        .status("PROVISIONED")
                        .build());

        AuthenticatedUser principal = new AuthenticatedUser(UUID.randomUUID(), "cajero@test.com", null);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CAJERO"))
        );

        mockMvc.perform(post("/api/v1/internal/users/provision-client-access")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.status").value("PROVISIONED"));
    }
}
