package com.udea.bancodigital.auth.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.bancodigital.auth.application.dto.ChangePasswordRequestDto;
import com.udea.bancodigital.auth.application.dto.ChangePasswordResponseDto;
import com.udea.bancodigital.auth.application.usecase.ChangePasswordUseCase;
import com.udea.bancodigital.auth.domain.port.in.AuthPort;
import com.udea.bancodigital.auth.infrastructure.config.JwtProvider;
import com.udea.bancodigital.shared.exception.GlobalExceptionHandler;
import com.udea.bancodigital.shared.exception.InvalidPasswordException;
import com.udea.bancodigital.shared.exception.PasswordChangeException;
import com.udea.bancodigital.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP verificado para Postman (rutas, status y JSON).
 */
@ExtendWith(MockitoExtension.class)
class ChangePasswordContractTest {

    private static final String CHANGE_PASSWORD_URL = "/api/v1/auth/change-password";

    @Mock
    private AuthPort authPort;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private ChangePasswordUseCase changePasswordUseCase;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private void authenticate(UUID userId) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, "admin@bancodigital.com", null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    }

    @Test
    @DisplayName("Escenario 1 Postman: 200 y cuerpo success/message")
    void escenario1_cambioExitoso() throws Exception {
        UUID userId = UUID.randomUUID();
        authenticate(userId);
        when(changePasswordUseCase.changePassword(eq(userId), any()))
                .thenReturn(ChangePasswordResponseDto.builder()
                        .success(true)
                        .message("Tu contraseña ha sido actualizada correctamente")
                        .build());

        ChangePasswordRequestDto body = ChangePasswordRequestDto.builder()
                .passwordActual("Admin123!")
                .passwordNueva("NuevaClave1!")
                .passwordConfirmacion("NuevaClave1!")
                .build();

        mockMvc.perform(post(CHANGE_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tu contraseña ha sido actualizada correctamente"));
    }

    @Test
    @DisplayName("Escenario 2 Postman: 400 INVALID_PASSWORD")
    void escenario2_contraseñaActualIncorrecta() throws Exception {
        UUID userId = UUID.randomUUID();
        authenticate(userId);
        when(changePasswordUseCase.changePassword(eq(userId), any()))
                .thenThrow(new InvalidPasswordException("La contraseña actual es incorrecta"));

        ChangePasswordRequestDto body = ChangePasswordRequestDto.builder()
                .passwordActual("EstaNoEsLaClave1!")
                .passwordNueva("OtraClave999!")
                .passwordConfirmacion("OtraClave999!")
                .build();

        mockMvc.perform(post(CHANGE_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.errorCode").value("INVALID_PASSWORD"))
                .andExpect(jsonPath("$.error.message").value("La contraseña actual es incorrecta"));
    }

    @Test
    @DisplayName("Escenario 3 Postman: 400 PASSWORD_CHANGE_ERROR igual a actual")
    void escenario3_nuevaIgualActual() throws Exception {
        UUID userId = UUID.randomUUID();
        authenticate(userId);
        when(changePasswordUseCase.changePassword(eq(userId), any()))
                .thenThrow(new PasswordChangeException("La nueva contraseña no puede ser igual a la actual"));

        ChangePasswordRequestDto body = ChangePasswordRequestDto.builder()
                .passwordActual("NuevaClave1!")
                .passwordNueva("NuevaClave1!")
                .passwordConfirmacion("NuevaClave1!")
                .build();

        mockMvc.perform(post(CHANGE_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.errorCode").value("PASSWORD_CHANGE_ERROR"))
                .andExpect(jsonPath("$.error.message").value("La nueva contraseña no puede ser igual a la actual"));
    }

    @Test
    @DisplayName("Escenario 4 Postman: 400 PASSWORD_CHANGE_ERROR historial")
    void escenario4_contraseñaEnHistorial() throws Exception {
        UUID userId = UUID.randomUUID();
        authenticate(userId);
        when(changePasswordUseCase.changePassword(eq(userId), any()))
                .thenThrow(new PasswordChangeException(
                        "Esta contraseña ya fue utilizada anteriormente. Por favor, cree una nueva."));

        ChangePasswordRequestDto body = ChangePasswordRequestDto.builder()
                .passwordActual("NuevaClave4!")
                .passwordNueva("NuevaClave2!")
                .passwordConfirmacion("NuevaClave2!")
                .build();

        mockMvc.perform(post(CHANGE_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.errorCode").value("PASSWORD_CHANGE_ERROR"))
                .andExpect(jsonPath("$.error.message")
                        .value("Esta contraseña ya fue utilizada anteriormente. Por favor, cree una nueva."));
    }
}
