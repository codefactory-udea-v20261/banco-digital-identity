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
}
