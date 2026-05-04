package com.udea.bancodigital.auth.infrastructure.adapter.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTokenBlacklistAdapterTest {

    private InMemoryTokenBlacklistAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InMemoryTokenBlacklistAdapter();
    }

    @Test
    @DisplayName("Debe retornar false cuando token no está revocado")
    void isRevoked_ShouldReturnFalseForNewToken() {
        assertThat(adapter.isRevoked("unknown-token")).isFalse();
    }

    @Test
    @DisplayName("Debe retornar true cuando token está revocado y no expirado")
    void isRevoked_ShouldReturnTrueForRevokedToken() {
        Instant futureExpiration = Instant.now().plusSeconds(3600);
        adapter.revoke("token-1", UUID.randomUUID(), futureExpiration);

        assertThat(adapter.isRevoked("token-1")).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false y limpiar token expirado")
    void isRevoked_ShouldReturnFalseAndCleanExpiredToken() {
        Instant pastExpiration = Instant.now().minusSeconds(3600);
        adapter.revoke("expired-token", UUID.randomUUID(), pastExpiration);

        assertThat(adapter.isRevoked("expired-token")).isFalse();
    }

    @Test
    @DisplayName("Debe revocar token exitosamente")
    void revoke_ShouldAddTokenToBlacklist() {
        adapter.revoke("new-token", UUID.randomUUID(), Instant.now().plusSeconds(3600));

        assertThat(adapter.isRevoked("new-token")).isTrue();
    }

    @Test
    @DisplayName("Debe limpiar tokens expirados")
    void cleanExpiredTokens_ShouldRemoveExpiredTokens() {
        adapter.revoke("valid", UUID.randomUUID(), Instant.now().plusSeconds(3600));
        adapter.revoke("expired1", UUID.randomUUID(), Instant.now().minusSeconds(100));
        adapter.revoke("expired2", UUID.randomUUID(), Instant.now().minusSeconds(200));

        adapter.cleanExpiredTokens();

        assertThat(adapter.isRevoked("valid")).isTrue();
        assertThat(adapter.isRevoked("expired1")).isFalse();
        assertThat(adapter.isRevoked("expired2")).isFalse();
    }
}
