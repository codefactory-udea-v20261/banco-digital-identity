package com.udea.bancodigital.auth.infrastructure.adapter.in.web;

import com.udea.bancodigital.auth.application.dto.*;
import com.udea.bancodigital.auth.application.usecase.ChangePasswordUseCase;
import com.udea.bancodigital.auth.domain.port.in.AuthPort;
import com.udea.bancodigital.auth.infrastructure.config.JwtProvider;
import com.udea.bancodigital.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthPort authPort;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private ChangePasswordUseCase changePasswordUseCase;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe retornar 200 con login exitoso")
    void login_ShouldReturn200() {
        LoginRequestDto request = LoginRequestDto.builder()
                .correo("user@test.com").clave("pass").build();
        LoginResponseDto responseDto = LoginResponseDto.builder()
                .token("token").refreshToken("refresh").build();

        when(authPort.login(request)).thenReturn(responseDto);

        ResponseEntity<LoginResponseDto> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getToken()).isEqualTo("token");
    }

    @Test
    @DisplayName("Debe retornar 200 con refresh exitoso")
    void refresh_ShouldReturn200() {
        RefreshRequestDto request = RefreshRequestDto.builder().refreshToken("refresh-token").build();
        LoginResponseDto responseDto = LoginResponseDto.builder()
                .token("new-token").refreshToken("new-refresh").build();

        when(authPort.refresh(request)).thenReturn(responseDto);

        ResponseEntity<LoginResponseDto> response = authController.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getToken()).isEqualTo("new-token");
    }

    @Test
    @DisplayName("Debe retornar 204 con logout exitoso")
    void logout_ShouldReturn204() {
        ResponseEntity<Void> response = authController.logout("Bearer token123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authPort).logout(any(LogoutRequestDto.class));
    }

    @Test
    @DisplayName("Debe validar token válido y retornar claims")
    void validateToken_WithValidToken_ShouldReturnActive() {
        String token = "valid-token";
        var claims = mock(io.jsonwebtoken.Claims.class);

        when(jwtProvider.isTokenValid(token)).thenReturn(true);
        when(jwtProvider.getClaims(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("roles")).thenReturn(java.util.List.of("ADMIN"));
        when(claims.get("permissions")).thenReturn(java.util.List.of("READ_ALL"));
        when(claims.get("clienteId")).thenReturn("cliente-123");
        when(claims.get("uid")).thenReturn("uid-456");

        ResponseEntity<TokenValidationResponseDto> response = authController.validateToken(token);

        assertThat(response.getBody().isActive()).isTrue();
        assertThat(response.getBody().getSub()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Debe retornar inactive para token inválido")
    void validateToken_WithInvalidToken_ShouldReturnInactive() {
        when(jwtProvider.isTokenValid("bad-token")).thenReturn(false);

        ResponseEntity<TokenValidationResponseDto> response = authController.validateToken("bad-token");

        assertThat(response.getBody().isActive()).isFalse();
    }

    @Test
    @DisplayName("Debe manejar token con comillas")
    void validateToken_WithQuotedToken_ShouldTrimQuotes() {
        when(jwtProvider.isTokenValid("actual-token")).thenReturn(false);

        ResponseEntity<TokenValidationResponseDto> response = authController.validateToken("\"actual-token\"");

        assertThat(response.getBody().isActive()).isFalse();
    }

    @Test
    @DisplayName("Debe retornar inactive cuando se lanza excepción")
    void validateToken_WhenException_ShouldReturnInactive() {
        when(jwtProvider.isTokenValid("crash")).thenThrow(new RuntimeException("bad"));

        ResponseEntity<TokenValidationResponseDto> response = authController.validateToken("crash");

        assertThat(response.getBody().isActive()).isFalse();
    }

    @Test
    @DisplayName("Debe cambiar contraseña con usuario autenticado")
    void changePassword_WithAuthenticatedUser_ShouldReturn200() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, "user@test.com", null);
        var auth = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ChangePasswordRequestDto request = ChangePasswordRequestDto.builder()
                .passwordActual("old").passwordNueva("new").passwordConfirmacion("new").build();
        ChangePasswordResponseDto responseDto = ChangePasswordResponseDto.builder()
                .success(true).message("OK").build();

        when(changePasswordUseCase.changePassword(eq(userId), any())).thenReturn(responseDto);

        ResponseEntity<ChangePasswordResponseDto> response = authController.changePassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar 401 cuando no hay autenticación")
    void changePassword_WithoutAuth_ShouldReturn401() {
        ChangePasswordRequestDto request = ChangePasswordRequestDto.builder()
                .passwordActual("old").passwordNueva("new").passwordConfirmacion("new").build();

        ResponseEntity<ChangePasswordResponseDto> response = authController.changePassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static io.jsonwebtoken.Claims mock(Class<io.jsonwebtoken.Claims> clazz) {
        return org.mockito.Mockito.mock(clazz);
    }
}
