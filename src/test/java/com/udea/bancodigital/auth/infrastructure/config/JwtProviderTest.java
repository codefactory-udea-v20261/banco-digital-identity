package com.udea.bancodigital.auth.infrastructure.config;

import com.udea.bancodigital.auth.domain.model.Permiso;
import com.udea.bancodigital.auth.domain.model.Rol;
import com.udea.bancodigital.auth.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private final String secret = "my-32-character-ultra-secure-and-ultra-long-secret-key-for-testing";

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtProvider, "jwtExpirationMs", 3600000);
        ReflectionTestUtils.setField(jwtProvider, "jwtRefreshExpirationMs", 86400000);
    }

    private Usuario buildUsuario(boolean withPermisos, boolean withClienteId) {
        Set<Permiso> permisos = withPermisos
                ? Set.of(Permiso.builder().id((short) 1).nombre("READ_ALL").build())
                : Set.of();
        Set<Rol> roles = Set.of(Rol.builder().id((short) 1).nombre("ADMIN").permisos(permisos).build());
        return Usuario.builder()
                .id(UUID.randomUUID())
                .clienteId(withClienteId ? UUID.randomUUID() : null)
                .correo("user@test.com")
                .clave("hash")
                .activo(true)
                .bloqueado(false)
                .intentosFallidos(0)
                .mfaActivo(false)
                .roles(roles)
                .build();
    }

    @Test
    @DisplayName("Debe generar token con claims correctos")
    void generateToken_ShouldContainCorrectClaims() {
        Usuario usuario = buildUsuario(true, true);
        String token = jwtProvider.generateToken(usuario);

        assertThat(token).isNotBlank();

        Claims claims = jwtProvider.getClaims(token);
        assertThat(claims.getSubject()).isEqualTo("user@test.com");
        assertThat(claims.get("uid")).isEqualTo(usuario.getId().toString());
        assertThat(claims.get("clienteId")).isEqualTo(usuario.getClienteId().toString());
        assertThat(claims.get("activo")).isEqualTo(true);
        assertThat(claims.get("bloqueado")).isEqualTo(false);
        assertThat(claims.get("mfaActivo")).isEqualTo(false);
        assertThat(claims.getId()).isNotBlank(); // jti
    }

    @Test
    @DisplayName("Debe generar token sin clienteId cuando es null")
    void generateToken_ShouldOmitClienteIdWhenNull() {
        Usuario usuario = buildUsuario(false, false);
        String token = jwtProvider.generateToken(usuario);

        Claims claims = jwtProvider.getClaims(token);
        assertThat(claims.get("clienteId")).isNull();
    }

    @Test
    @DisplayName("Debe generar refresh token")
    void generateRefreshToken_ShouldReturnValidToken() {
        Usuario usuario = buildUsuario(false, false);
        String refreshToken = jwtProvider.generateRefreshToken(usuario);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtProvider.isTokenValid(refreshToken)).isTrue();
        assertThat(jwtProvider.extractUsername(refreshToken)).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Debe extraer JTI del token")
    void extractJti_ShouldReturnJti() {
        Usuario usuario = buildUsuario(false, false);
        String token = jwtProvider.generateToken(usuario);

        String jti = jwtProvider.extractJti(token);
        assertThat(jti).isNotBlank();
    }

    @Test
    @DisplayName("Debe extraer userId del token")
    void extractUserId_ShouldReturnUserId() {
        Usuario usuario = buildUsuario(false, true);
        String token = jwtProvider.generateToken(usuario);

        UUID userId = jwtProvider.extractUserId(token);
        assertThat(userId).isEqualTo(usuario.getId());
    }

    @Test
    @DisplayName("Debe extraer username del token")
    void extractUsername_ShouldReturnEmail() {
        Usuario usuario = buildUsuario(false, false);
        String token = jwtProvider.generateToken(usuario);

        assertThat(jwtProvider.extractUsername(token)).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Debe extraer fecha de expiración del token")
    void extractExpiration_ShouldReturnFutureDate() {
        Usuario usuario = buildUsuario(false, false);
        String token = jwtProvider.generateToken(usuario);

        Date exp = jwtProvider.extractExpiration(token);
        assertThat(exp).isAfter(new Date());
    }

    @Test
    @DisplayName("Debe validar token válido")
    void isTokenValid_ShouldReturnTrueForValidToken() {
        Usuario usuario = buildUsuario(false, false);
        String token = jwtProvider.generateToken(usuario);

        assertThat(jwtProvider.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Debe invalidar token malformado")
    void isTokenValid_ShouldReturnFalseForInvalidToken() {
        assertThat(jwtProvider.isTokenValid("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("Debe retornar tiempo de expiración configurado")
    void getExpirationTime_ShouldReturnConfiguredValue() {
        assertThat(jwtProvider.getExpirationTime()).isEqualTo(3600000);
    }

    @Test
    @DisplayName("Debe extraer clienteId del token")
    void extractClienteId_ShouldReturnClienteId() {
        Usuario usuario = buildUsuario(false, true);
        String token = jwtProvider.generateToken(usuario);

        UUID clienteId = jwtProvider.extractClienteId(token);
        assertThat(clienteId).isEqualTo(usuario.getClienteId());
    }

    @Test
    @DisplayName("Debe retornar null cuando clienteId no existe en token")
    void extractClienteId_ShouldReturnNullWhenMissing() {
        Usuario usuario = buildUsuario(false, false);
        String token = jwtProvider.generateToken(usuario);

        UUID clienteId = jwtProvider.extractClienteId(token);
        assertThat(clienteId).isNull();
    }

    @Test
    @DisplayName("Debe incluir permisos en claims del token")
    void generateToken_ShouldIncludePermissions() {
        Usuario usuario = buildUsuario(true, false);
        String token = jwtProvider.generateToken(usuario);

        Claims claims = jwtProvider.getClaims(token);
        assertThat(claims.get("permissions")).isNotNull();
    }
}
