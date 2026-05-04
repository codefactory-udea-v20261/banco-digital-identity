package com.udea.bancodigital.auth.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    void shouldTestGettersAndBuilder() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Set<Rol> roles = Set.of(Rol.builder().id((short) 1).nombre("ADMIN").build());

        Usuario usuario = Usuario.builder()
                .id(id)
                .clienteId(clienteId)
                .correo("test@test.com")
                .clave("hash")
                .activo(true)
                .bloqueado(false)
                .intentosFallidos(2)
                .secretoMfa("secret")
                .mfaActivo(true)
                .roles(roles)
                .build();

        assertThat(usuario.getId()).isEqualTo(id);
        assertThat(usuario.getClienteId()).isEqualTo(clienteId);
        assertThat(usuario.getCorreo()).isEqualTo("test@test.com");
        assertThat(usuario.getClave()).isEqualTo("hash");
        assertThat(usuario.isActivo()).isTrue();
        assertThat(usuario.isBloqueado()).isFalse();
        assertThat(usuario.getIntentosFallidos()).isEqualTo(2);
        assertThat(usuario.getSecretoMfa()).isEqualTo("secret");
        assertThat(usuario.isMfaActivo()).isTrue();
        assertThat(usuario.getRoles()).isEqualTo(roles);

        // @Value classes are immutable - test different state via new builder
        Usuario usuario2 = Usuario.builder()
                .id(id)
                .clienteId(clienteId)
                .correo("new@test.com")
                .clave("hash")
                .activo(false)
                .bloqueado(true)
                .intentosFallidos(3)
                .build();

        assertThat(usuario2.getCorreo()).isEqualTo("new@test.com");
        assertThat(usuario2.isActivo()).isFalse();
        assertThat(usuario2.isBloqueado()).isTrue();
        assertThat(usuario2.getIntentosFallidos()).isEqualTo(3);

        // toString, equals, hashCode
        assertThat(usuario.toString()).isNotBlank();
        assertThat(usuario.hashCode()).isNotZero();
        assertThat(usuario).isEqualTo(usuario);
        assertThat(usuario).isNotEqualTo(new Object());
        assertThat(usuario).isNotEqualTo(usuario2);
    }
}
