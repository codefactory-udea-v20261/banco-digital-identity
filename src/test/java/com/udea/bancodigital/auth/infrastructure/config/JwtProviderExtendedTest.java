package com.udea.bancodigital.auth.infrastructure.config;

import com.udea.bancodigital.auth.domain.model.Permiso;
import com.udea.bancodigital.auth.domain.model.Rol;
import com.udea.bancodigital.auth.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtProvider - Extended")
class JwtProviderExtendedTest {
    private JwtProvider jwtProvider;
    private final String secret = "my-32-character-ultra-secure-and-ultra-long-secret-key-for-testing";

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtProvider, "jwtExpirationMs", 3600000);
        ReflectionTestUtils.setField(jwtProvider, "jwtRefreshExpirationMs", 86400000);
    }

    private Usuario buildUsuario(UUID clienteId) {
        Set<Permiso> permisos = Set.of(
                Permiso.builder().id((short) 1).nombre("PERM_MANAGE_CLIENTS").build());
        Set<Rol> roles = Set.of(
                Rol.builder().id((short) 1).nombre("ADMIN").permisos(permisos).build());
        return Usuario.builder()
                .id(UUID.randomUUID())
                .clienteId(clienteId)
                .correo("test@banco.com")
                .clave("hash")
                .activo(true)
                .bloqueado(false)
                .intentosFallidos(0)
                .mfaActivo(false)
                .roles(roles)
                .build();
    }

    @Nested
    @DisplayName("extractClienteId()")
    class ExtractClienteIdTest {

        @Test
        @DisplayName("Debe extraer clienteId correctamente del token")
        void debeExtraerClienteId() {
            // Arrange
            UUID clienteId = UUID.randomUUID();
            Usuario usuario = buildUsuario(clienteId);

            // Act
            String token = jwtProvider.generateToken(usuario);
            UUID extraido = jwtProvider.extractClienteId(token);

            // Assert
            assertThat(extraido).isEqualTo(clienteId);
        }

        @Test
        @DisplayName("Debe retornar null cuando el token no tiene clienteId")
        void debeRetornarNullSinClienteId() {
            // Arrange
            Usuario usuario = buildUsuario(null);

            // Act
            String token = jwtProvider.generateToken(usuario);
            UUID extraido = jwtProvider.extractClienteId(token);

            // Assert
            assertThat(extraido).isNull();
        }
    }

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsernameTest {

        @Test
        @DisplayName("Debe extraer el correo como username del token")
        void debeExtraerCorreoComoUsername() {
            // Arrange
            Usuario usuario = buildUsuario(UUID.randomUUID());

            // Act
            String token = jwtProvider.generateToken(usuario);
            String username = jwtProvider.extractUsername(token);

            // Assert
            assertThat(username).isEqualTo("test@banco.com");
        }
    }

    @Nested
    @DisplayName("extractExpiration()")
    class ExtractExpirationTest {

        @Test
        @DisplayName("Debe extraer la fecha de expiración del token")
        void debeExtraerFechaExpiracion() {
            // Arrange
            Usuario usuario = buildUsuario(UUID.randomUUID());

            // Act
            String token = jwtProvider.generateToken(usuario);

            // Assert
            assertThat(jwtProvider.extractExpiration(token)).isNotNull();
            assertThat(jwtProvider.extractExpiration(token)).isInTheFuture();
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValidTest {

        @Test
        @DisplayName("Debe retornar true para token recién generado")
        void debeRetornarTrueParaTokenValido() {
            // Arrange
            Usuario usuario = buildUsuario(UUID.randomUUID());

            // Act
            String token = jwtProvider.generateToken(usuario);

            // Assert
            assertThat(jwtProvider.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false para token malformado")
        void debeRetornarFalseParaTokenMalformado() {
            // Arrange / Act / Assert
            assertThat(jwtProvider.isTokenValid("token.invalido.malformado")).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false para string vacío")
        void debeRetornarFalseParaStringVacio() {
            // Arrange / Act / Assert
            assertThat(jwtProvider.isTokenValid("")).isFalse();
        }
    }

    @Nested
    @DisplayName("generateRefreshToken()")
    class GenerateRefreshTokenTest {

        @Test
        @DisplayName("Debe generar refresh token diferente al access token")
        void debeGenerarRefreshTokenDiferente() {
            // Arrange
            Usuario usuario = buildUsuario(UUID.randomUUID());

            // Act
            String accessToken = jwtProvider.generateToken(usuario);
            String refreshToken = jwtProvider.generateRefreshToken(usuario);

            // Assert
            assertThat(refreshToken).isNotBlank();
            assertThat(refreshToken).isNotEqualTo(accessToken);
        }

        @Test
        @DisplayName("El refresh token debe tener el correo como subject")
        void debeContenerCorreoComoSubject() {
            // Arrange
            Usuario usuario = buildUsuario(UUID.randomUUID());

            // Act
            String refreshToken = jwtProvider.generateRefreshToken(usuario);
            String subject = jwtProvider.extractUsername(refreshToken);

            // Assert
            assertThat(subject).isEqualTo("test@banco.com");
        }
    }

    @Nested
    @DisplayName("getExpirationTime()")
    class GetExpirationTimeTest {

        @Test
        @DisplayName("Debe retornar el tiempo de expiración configurado")
        void debeRetornarTiempoConfigurrado() {
            // Arrange / Act
            long expiration = jwtProvider.getExpirationTime();

            // Assert
            assertThat(expiration).isEqualTo(3600000L);
        }
    }

    @Nested
    @DisplayName("Usuario sin roles con permisos")
    class UsuarioSinPermisosTest {

        @Test
        @DisplayName("Debe generar token aunque los permisos del rol sean null")
        void debeGenerarTokenConRolesSinPermisos() {
            // Arrange
            Set<Rol> roles = Set.of(
                    Rol.builder().id((short) 1).nombre("CLIENTE").permisos(null).build());
            Usuario usuario = Usuario.builder()
                    .id(UUID.randomUUID())
                    .correo("cliente@banco.com")
                    .clave("hash")
                    .activo(true)
                    .bloqueado(false)
                    .mfaActivo(false)
                    .roles(roles)
                    .build();

            // Act
            String token = jwtProvider.generateToken(usuario);

            // Assert
            assertThat(token).isNotBlank();
            assertThat(jwtProvider.isTokenValid(token)).isTrue();
        }
    }

}
