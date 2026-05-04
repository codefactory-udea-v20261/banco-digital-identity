package com.udea.bancodigital.auth.application.usecase;

import com.udea.bancodigital.auth.application.dto.LoginRequestDto;
import com.udea.bancodigital.auth.application.dto.LoginResponseDto;
import com.udea.bancodigital.auth.application.dto.LogoutRequestDto;
import com.udea.bancodigital.auth.domain.model.Rol;
import com.udea.bancodigital.auth.domain.model.Usuario;
import com.udea.bancodigital.auth.domain.port.out.JwtProviderPort;
import com.udea.bancodigital.auth.domain.port.out.PasswordEncoderPort;
import com.udea.bancodigital.auth.domain.port.out.TokenBlacklistPort;
import com.udea.bancodigital.auth.domain.port.out.UsuarioRepositoryPort;
import com.udea.bancodigital.shared.exception.CredencialesInvalidasException;
import com.udea.bancodigital.shared.exception.CuentaBloqueadaException;
import com.udea.bancodigital.shared.exception.MfaRequeridoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthUseCase")
class AuthUseCaseTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private JwtProviderPort jwtProviderPort;

    @Mock
    private TokenBlacklistPort tokenBlacklistPort;

    @InjectMocks
    private AuthUseCase authUseCase;

    @Test
    @DisplayName("Debe permitir login exitoso cuando las credenciales son correctas")
    void debePermitirLoginExitoso() {
        Usuario usuario = usuarioCliente(false, 0);
        LoginRequestDto request = LoginRequestDto.builder()
                .correo(usuario.getCorreo())
                .clave("Test1234!")
                .build();

        when(usuarioRepositoryPort.findByEmail(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoderPort.matches("Test1234!", usuario.getClave())).thenReturn(true);
        when(jwtProviderPort.generateToken(usuario)).thenReturn("jwt-token");

        LoginResponseDto response = authUseCase.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(usuarioRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar login con credenciales inválidas y aumentar intentos fallidos")
    void debeRechazarCredencialesInvalidasYAumentarIntentos() {
        Usuario usuario = usuarioCliente(false, 1);
        LoginRequestDto request = LoginRequestDto.builder()
                .correo(usuario.getCorreo())
                .clave("mala")
                .build();

        when(usuarioRepositoryPort.findByEmail(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoderPort.matches("mala", usuario.getClave())).thenReturn(false);

        assertThatThrownBy(() -> authUseCase.login(request))
                .isInstanceOf(CredencialesInvalidasException.class);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositoryPort).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getIntentosFallidos()).isEqualTo(2);
        assertThat(usuarioCaptor.getValue().isBloqueado()).isFalse();
    }

    @Test
    @DisplayName("Debe bloquear la cuenta al tercer intento fallido")
    void debeBloquearCuentaAlTercerIntentoFallido() {
        Usuario usuario = usuarioCliente(false, 2);
        LoginRequestDto request = LoginRequestDto.builder()
                .correo(usuario.getCorreo())
                .clave("mala")
                .build();

        when(usuarioRepositoryPort.findByEmail(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoderPort.matches("mala", usuario.getClave())).thenReturn(false);

        assertThatThrownBy(() -> authUseCase.login(request))
                .isInstanceOf(CuentaBloqueadaException.class);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositoryPort).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getIntentosFallidos()).isEqualTo(3);
        assertThat(usuarioCaptor.getValue().isBloqueado()).isTrue();
    }

    @Test
    @DisplayName("Debe reiniciar intentos si el bloqueo ya expiró antes de autenticar")
    void debeReiniciarIntentosSiBloqueoYaExpiró() {
        Usuario usuario = usuarioCliente(false, 3);
        LoginRequestDto request = LoginRequestDto.builder()
                .correo(usuario.getCorreo())
                .clave("Test1234!")
                .build();

        when(usuarioRepositoryPort.findByEmail(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoderPort.matches("Test1234!", usuario.getClave())).thenReturn(true);
        when(jwtProviderPort.generateToken(any(Usuario.class))).thenReturn("jwt-token");

        LoginResponseDto response = authUseCase.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositoryPort).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getIntentosFallidos()).isZero();
        assertThat(usuarioCaptor.getValue().isBloqueado()).isFalse();
    }

    @Test
    @DisplayName("Debe requerir MFA solo cuando el usuario privilegiado lo tiene activo")
    void debeRequerirMfaSoloCuandoEstaActivo() {
        Usuario adminSinMfa = usuarioPrivilegiado(false);
        LoginRequestDto loginSinMfa = LoginRequestDto.builder()
                .correo(adminSinMfa.getCorreo())
                .clave("Test1234!")
                .build();

        when(usuarioRepositoryPort.findByEmail(adminSinMfa.getCorreo())).thenReturn(Optional.of(adminSinMfa));
        when(passwordEncoderPort.matches("Test1234!", adminSinMfa.getClave())).thenReturn(true);
        when(jwtProviderPort.generateToken(adminSinMfa)).thenReturn("jwt-admin");

        LoginResponseDto response = authUseCase.login(loginSinMfa);

        assertThat(response.getToken()).isEqualTo("jwt-admin");

        Usuario adminConMfa = usuarioPrivilegiado(true);
        LoginRequestDto loginSinCodigo = LoginRequestDto.builder()
                .correo(adminConMfa.getCorreo())
                .clave("Test1234!")
                .build();

        when(usuarioRepositoryPort.findByEmail(adminConMfa.getCorreo())).thenReturn(Optional.of(adminConMfa));

        assertThatThrownBy(() -> authUseCase.login(loginSinCodigo))
                .isInstanceOf(MfaRequeridoException.class);
    }

    @Test
    @DisplayName("Debe revocar el token con su expiración real durante logout")
    void debeRevocarTokenConExpiracionRealEnLogout() {
        Instant expiration = Instant.parse("2026-04-01T18:00:00Z");
        UUID usuarioId = UUID.randomUUID();
        LogoutRequestDto request = new LogoutRequestDto();
        request.setToken("Bearer jwt-token");

        when(jwtProviderPort.extractJti("jwt-token")).thenReturn("jti-123");
        when(jwtProviderPort.extractUserId("jwt-token")).thenReturn(usuarioId);
        when(jwtProviderPort.extractExpiration("jwt-token")).thenReturn(Date.from(expiration));

        authUseCase.logout(request);

        verify(tokenBlacklistPort).revoke("jti-123", usuarioId, expiration);
    }

    private Usuario usuarioCliente(boolean bloqueado, int intentos) {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .clienteId(UUID.randomUUID())
                .correo("cliente@test.com")
                .clave("$2a$12$hash")
                .activo(true)
                .bloqueado(bloqueado)
                .intentosFallidos(intentos)
                .mfaActivo(false)
                .roles(Set.of(rol((short) 3, "CLIENTE")))
                .build();
    }

    private Usuario usuarioPrivilegiado(boolean mfaActivo) {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .correo("admin@banco.com")
                .clave("$2a$12$hash")
                .activo(true)
                .bloqueado(false)
                .intentosFallidos(0)
                .secretoMfa("123456")
                .mfaActivo(mfaActivo)
                .roles(Set.of(rol((short) 1, "ADMIN")))
                .build();
    }

    private Rol rol(short id, String nombre) {
        return Rol.builder()
                .id(id)
                .nombre(nombre)
                .build();
    }

    @Test
    @DisplayName("Debe permitir login exitoso con MFA correcto para rol ADMIN")
    void debePermitirLoginExitosoConMfaCorrecto() {
        Usuario adminConMfa = usuarioPrivilegiado(true);
        LoginRequestDto request = LoginRequestDto.builder()
                .correo(adminConMfa.getCorreo())
                .clave("Test1234!")
                .mfaCode("123456")
                .build();

        when(usuarioRepositoryPort.findByEmail(adminConMfa.getCorreo())).thenReturn(Optional.of(adminConMfa));
        when(passwordEncoderPort.matches("Test1234!", adminConMfa.getClave())).thenReturn(true);
        when(jwtProviderPort.generateToken(adminConMfa)).thenReturn("jwt-admin-mfa");

        LoginResponseDto response = authUseCase.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-admin-mfa");
    }

    @Test
    @DisplayName("Debe rechazar login si el MFA proporcionado es incorrecto o vacío")
    void debeRechazarMfaIncorrectoOVacío() {
        Usuario adminConMfa = usuarioPrivilegiado(true);

        LoginRequestDto requestMfaIncorrecto = LoginRequestDto.builder()
                .correo(adminConMfa.getCorreo())
                .clave("Test1234!")
                .mfaCode("999999")
                .build();

        when(usuarioRepositoryPort.findByEmail(adminConMfa.getCorreo())).thenReturn(Optional.of(adminConMfa));
        when(passwordEncoderPort.matches("Test1234!", adminConMfa.getClave())).thenReturn(true);

        assertThatThrownBy(() -> authUseCase.login(requestMfaIncorrecto))
                .isInstanceOf(MfaRequeridoException.class);

        LoginRequestDto requestMfaVacio = LoginRequestDto.builder()
                .correo(adminConMfa.getCorreo())
                .clave("Test1234!")
                .mfaCode("  ")
                .build();

        assertThatThrownBy(() -> authUseCase.login(requestMfaVacio))
                .isInstanceOf(MfaRequeridoException.class);
    }

    @Test
    @DisplayName("Debe requerir MFA para rol AUDITOR")
    void debeRequerirMfaParaAuditor() {
        Usuario auditorConMfa = Usuario.builder()
                .id(UUID.randomUUID())
                .correo("auditor@banco.com")
                .clave("$2a$12$hash")
                .activo(true)
                .bloqueado(false)
                .intentosFallidos(0)
                .secretoMfa("654321")
                .mfaActivo(true)
                .roles(Set.of(rol((short) 2, "AUDITOR")))
                .build();

        LoginRequestDto requestSinMfa = LoginRequestDto.builder()
                .correo(auditorConMfa.getCorreo())
                .clave("Test1234!")
                .build();

        when(usuarioRepositoryPort.findByEmail(auditorConMfa.getCorreo())).thenReturn(Optional.of(auditorConMfa));
        when(passwordEncoderPort.matches("Test1234!", auditorConMfa.getClave())).thenReturn(true);

        assertThatThrownBy(() -> authUseCase.login(requestSinMfa))
                .isInstanceOf(MfaRequeridoException.class);
    }

    @Test
    @DisplayName("Debe permitir logout exitoso con token sin prefijo Bearer")
    void debePermitirLogoutExitosoSinBearer() {
        Instant expiration = Instant.parse("2026-04-01T18:00:00Z");
        UUID usuarioId = UUID.randomUUID();
        LogoutRequestDto request = new LogoutRequestDto();
        request.setToken("jwt-token");

        when(jwtProviderPort.extractJti("jwt-token")).thenReturn("jti-123");
        when(jwtProviderPort.extractUserId("jwt-token")).thenReturn(usuarioId);
        when(jwtProviderPort.extractExpiration("jwt-token")).thenReturn(Date.from(expiration));

        authUseCase.logout(request);

        verify(tokenBlacklistPort).revoke("jti-123", usuarioId, expiration);
    }

    @Test
    @DisplayName("Debe fallar al intentar login si el usuario está inactivo pero no bloqueado")
    void debeFallarLoginUsuarioInactivo() {
        Usuario inactivo = Usuario.builder()
                .id(UUID.randomUUID())
                .correo("cliente@test.com")
                .clave("$2a$12$hash")
                .activo(false)
                .bloqueado(false)
                .intentosFallidos(0)
                .build();

        LoginRequestDto request = LoginRequestDto.builder()
                .correo(inactivo.getCorreo())
                .clave("Test1234!")
                .build();

        when(usuarioRepositoryPort.findByEmail(inactivo.getCorreo())).thenReturn(Optional.of(inactivo));

        assertThatThrownBy(() -> authUseCase.login(request))
                .isInstanceOf(CuentaBloqueadaException.class);
    }

    @Test
    @DisplayName("Debe fallar refresh si el token es inválido")
    void debeFallarRefreshTokenInvalido() {
        com.udea.bancodigital.auth.application.dto.RefreshRequestDto request = new com.udea.bancodigital.auth.application.dto.RefreshRequestDto("invalid-refresh-token");
        when(jwtProviderPort.isTokenValid("invalid-refresh-token")).thenReturn(false);

        assertThatThrownBy(() -> authUseCase.refresh(request))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("Debe fallar refresh si el token está revocado")
    void debeFallarRefreshTokenRevocado() {
        com.udea.bancodigital.auth.application.dto.RefreshRequestDto request = new com.udea.bancodigital.auth.application.dto.RefreshRequestDto("revoked-refresh-token");
        when(jwtProviderPort.isTokenValid("revoked-refresh-token")).thenReturn(true);
        when(jwtProviderPort.extractJti("revoked-refresh-token")).thenReturn("jti-123");
        when(tokenBlacklistPort.isRevoked("jti-123")).thenReturn(true);

        assertThatThrownBy(() -> authUseCase.refresh(request))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("Debe fallar refresh si el usuario no existe")
    void debeFallarRefreshUsuarioNoExiste() {
        com.udea.bancodigital.auth.application.dto.RefreshRequestDto request = new com.udea.bancodigital.auth.application.dto.RefreshRequestDto("valid-refresh-token");
        when(jwtProviderPort.isTokenValid("valid-refresh-token")).thenReturn(true);
        when(jwtProviderPort.extractJti("valid-refresh-token")).thenReturn("jti-123");
        when(tokenBlacklistPort.isRevoked("jti-123")).thenReturn(false);
        when(jwtProviderPort.extractUsername("valid-refresh-token")).thenReturn("user@test.com");
        when(usuarioRepositoryPort.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authUseCase.refresh(request))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("Debe realizar refresh correctamente")
    void debeRealizarRefreshCorrectamente() {
        com.udea.bancodigital.auth.application.dto.RefreshRequestDto request = new com.udea.bancodigital.auth.application.dto.RefreshRequestDto("valid-refresh-token");
        Usuario usuario = usuarioCliente(false, 0);
        Instant expiration = Instant.parse("2026-04-01T18:00:00Z");

        when(jwtProviderPort.isTokenValid("valid-refresh-token")).thenReturn(true);
        when(jwtProviderPort.extractJti("valid-refresh-token")).thenReturn("jti-123");
        when(tokenBlacklistPort.isRevoked("jti-123")).thenReturn(false);
        when(jwtProviderPort.extractUsername("valid-refresh-token")).thenReturn(usuario.getCorreo());
        when(usuarioRepositoryPort.findByEmail(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(jwtProviderPort.extractExpiration("valid-refresh-token")).thenReturn(Date.from(expiration));
        when(jwtProviderPort.generateToken(usuario)).thenReturn("new-access-token");
        when(jwtProviderPort.generateRefreshToken(usuario)).thenReturn("new-refresh-token");

        LoginResponseDto response = authUseCase.refresh(request);

        assertThat(response.getToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(tokenBlacklistPort).revoke("jti-123", usuario.getId(), expiration);
    }
}
