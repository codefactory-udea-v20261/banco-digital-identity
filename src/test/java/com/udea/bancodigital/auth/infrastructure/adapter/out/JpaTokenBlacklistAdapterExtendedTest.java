package com.udea.bancodigital.auth.infrastructure.adapter.out;

import com.udea.bancodigital.auth.infrastructure.entity.TokenRevocadoEntity;
import com.udea.bancodigital.auth.infrastructure.repository.TokenRevocadoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaTokenBlacklistAdapter - Extended")
class JpaTokenBlacklistAdapterExtendedTest {
    @Mock
    private TokenRevocadoJpaRepository repository;

    private JpaTokenBlacklistAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaTokenBlacklistAdapter(repository);
    }

    @Nested
    @DisplayName("isRevoked()")
    class IsRevokedTest {

        @Test
        @DisplayName("Debe retornar true cuando el token existe y no ha expirado")
        void debeRetornarTrueCuandoExisteYNoHaExpirado() {
            // Arrange
            String token = "token-activo";
            when(repository.existsByJtiAndExpiraAtAfter(eq(token), any())).thenReturn(true);

            // Act
            boolean revocado = adapter.isRevoked(token);

            // Assert
            assertThat(revocado).isTrue();
            verify(repository).existsByJtiAndExpiraAtAfter(eq(token), any());
        }

        @Test
        @DisplayName("Debe retornar false cuando el token no existe en la blacklist")
        void debeRetornarFalseCuandoNoExiste() {
            // Arrange
            String token = "token-nuevo";
            when(repository.existsByJtiAndExpiraAtAfter(eq(token), any())).thenReturn(false);

            // Act
            boolean revocado = adapter.isRevoked(token);

            // Assert
            assertThat(revocado).isFalse();
        }
    }

    @Nested
    @DisplayName("revoke()")
    class RevokeTest {

        @Test
        @DisplayName("Debe guardar el token cuando no existe previamente")
        void debeGuardarTokenCuandoNoExiste() {
            // Arrange
            String token = "nuevo-token";
            UUID usuarioId = UUID.randomUUID();
            Instant expiracion = Instant.now().plusSeconds(3600);
            when(repository.findByJti(token)).thenReturn(Optional.empty());

            // Act
            adapter.revoke(token, usuarioId, expiracion);

            // Assert
            ArgumentCaptor<TokenRevocadoEntity> captor = ArgumentCaptor.forClass(TokenRevocadoEntity.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getJti()).isEqualTo(token);
            assertThat(captor.getValue().getUsuarioId()).isEqualTo(usuarioId);
        }

        @Test
        @DisplayName("No debe guardar el token si ya existe en la blacklist")
        void noDebeGuardarSiYaExiste() {
            // Arrange
            String token = "token-existente";
            TokenRevocadoEntity existente = TokenRevocadoEntity.builder()
                    .jti(token)
                    .usuarioId(UUID.randomUUID())
                    .build();
            when(repository.findByJti(token)).thenReturn(Optional.of(existente));

            // Act
            adapter.revoke(token, UUID.randomUUID(), Instant.now().plusSeconds(3600));

            // Assert
            verify(repository, never()).save(any());
        }
    }

}
