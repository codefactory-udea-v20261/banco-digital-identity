package com.udea.bancodigital.auth.infrastructure.entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioEntityTest {

    @Test
    void shouldTestGettersAndSetters() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Set<RolEntity> roles = Set.of(RolEntity.builder().id((short) 1).nombre("ADMIN").build());
        OffsetDateTime date = OffsetDateTime.now().plusDays(1);

        UsuarioEntity entity = UsuarioEntity.builder()
                .id(id)
                .clienteId(clienteId)
                .correo("test@test.com")
                .clave("hash")
                .activo(true)
                .intentosFallidos((short) 2)
                .secretoMfa("secret")
                .mfaActivo(true)
                .bloqueadoHasta(date)
                .roles(roles)
                .build();

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getClienteId()).isEqualTo(clienteId);
        assertThat(entity.getCorreo()).isEqualTo("test@test.com");
        assertThat(entity.getClave()).isEqualTo("hash");
        assertThat(entity.isActivo()).isTrue();
        assertThat(entity.getIntentosFallidos()).isEqualTo((short) 2);
        assertThat(entity.getSecretoMfa()).isEqualTo("secret");
        assertThat(entity.isMfaActivo()).isTrue();
        assertThat(entity.getBloqueadoHasta()).isEqualTo(date);
        assertThat(entity.getRoles()).isEqualTo(roles);

        entity.setCorreo("new@test.com");
        assertThat(entity.getCorreo()).isEqualTo("new@test.com");
        
        assertThat(entity.isBloqueado()).isTrue();
        
        entity.setBloqueadoHasta(OffsetDateTime.now().minusDays(1));
        assertThat(entity.isBloqueado()).isFalse();

        assertThat(entity.toString()).isNotBlank();
        assertThat(entity.hashCode()).isNotZero();
        assertThat(entity).isEqualTo(entity);
        assertThat(entity).isNotEqualTo(new Object());
    }
}
