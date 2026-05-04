package com.udea.bancodigital.auth.infrastructure.entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenRevocadoEntityTest {

    @Test
    void shouldTestGettersAndSetters() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime revocadoAt = OffsetDateTime.now();
        OffsetDateTime expiraAt = OffsetDateTime.now().plusHours(1);
        
        TokenRevocadoEntity entity = TokenRevocadoEntity.builder()
                .jti("jti123")
                .usuarioId(userId)
                .revocadoAt(revocadoAt)
                .expiraAt(expiraAt)
                .build();

        assertThat(entity.getJti()).isEqualTo("jti123");
        assertThat(entity.getUsuarioId()).isEqualTo(userId);
        assertThat(entity.getRevocadoAt()).isEqualTo(revocadoAt);
        assertThat(entity.getExpiraAt()).isEqualTo(expiraAt);

        entity.setJti("newJti");
        assertThat(entity.getJti()).isEqualTo("newJti");

        assertThat(entity.toString()).isNotBlank();
        assertThat(entity.hashCode()).isNotZero();
        assertThat(entity).isEqualTo(entity);
        assertThat(entity).isNotEqualTo(new Object());
    }
}
