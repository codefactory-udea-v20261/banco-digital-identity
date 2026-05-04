package com.udea.bancodigital.shared.security;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedUserTest {
    @Test
    void shouldCreateRecord() {
        UUID userId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, "test@test.com", clienteId);
        assertThat(user.userId()).isEqualTo(userId);
        assertThat(user.username()).isEqualTo("test@test.com");
        assertThat(user.clienteId()).isEqualTo(clienteId);
    }

    @Test
    void shouldTestEquality() {
        UUID id = UUID.randomUUID();
        AuthenticatedUser u1 = new AuthenticatedUser(id, "user", null);
        AuthenticatedUser u2 = new AuthenticatedUser(id, "user", null);
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).hasSameHashCodeAs(u2);
        assertThat(u1.toString()).contains("user");
    }
}
