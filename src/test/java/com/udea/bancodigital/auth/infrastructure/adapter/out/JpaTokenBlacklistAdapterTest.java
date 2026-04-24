package com.udea.bancodigital.auth.infrastructure.adapter.out;

import com.udea.bancodigital.auth.infrastructure.entity.TokenRevocadoEntity;
import com.udea.bancodigital.auth.infrastructure.repository.TokenRevocadoJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaTokenBlacklistAdapter")
class JpaTokenBlacklistAdapterTest {

    @Mock
    private TokenRevocadoJpaRepository tokenRevocadoJpaRepository;

    @InjectMocks
    private JpaTokenBlacklistAdapter blacklistAdapter;

    @Test
    @DisplayName("Debe consultar si un JTI sigue revocado y no expirado")
    void debeConsultarSiJtiSigueRevocado() {
        when(tokenRevocadoJpaRepository.existsByJtiAndExpiraAtAfter(eq("jti-123"), any(OffsetDateTime.class)))
                .thenReturn(true);

        boolean revoked = blacklistAdapter.isRevoked("jti-123");

        assertThat(revoked).isTrue();
    }

    @Test
    @DisplayName("Debe persistir un token revocado con usuario y expiración real")
    void debePersistirTokenRevocado() {
        UUID usuarioId = UUID.randomUUID();
        Instant expiration = Instant.parse("2026-04-01T18:00:00Z");

        when(tokenRevocadoJpaRepository.findByJti("jti-123")).thenReturn(Optional.empty());

        blacklistAdapter.revoke("jti-123", usuarioId, expiration);

        ArgumentCaptor<TokenRevocadoEntity> entityCaptor = ArgumentCaptor.forClass(TokenRevocadoEntity.class);
        verify(tokenRevocadoJpaRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getJti()).isEqualTo("jti-123");
        assertThat(entityCaptor.getValue().getUsuarioId()).isEqualTo(usuarioId);
        assertThat(entityCaptor.getValue().getExpiraAt())
                .isEqualTo(OffsetDateTime.ofInstant(expiration, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("No debe duplicar un JTI ya revocado")
    void noDebeDuplicarJtiRevocado() {
        when(tokenRevocadoJpaRepository.findByJti("jti-123"))
                .thenReturn(Optional.of(TokenRevocadoEntity.builder().id(1L).jti("jti-123").build()));

        blacklistAdapter.revoke("jti-123", UUID.randomUUID(), Instant.now());

        verify(tokenRevocadoJpaRepository, never()).save(any());
    }
}
