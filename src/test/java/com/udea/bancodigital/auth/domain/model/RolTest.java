package com.udea.bancodigital.auth.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RolTest {

    @Test
    void shouldTestGettersAndBuilder() {
        Set<Permiso> permisos = Set.of(Permiso.builder().id((short) 1).nombre("P1").build());
        
        Rol rol = Rol.builder()
                .id((short) 1)
                .nombre("ADMIN")
                .permisos(permisos)
                .build();

        assertThat(rol.getId()).isEqualTo((short) 1);
        assertThat(rol.getNombre()).isEqualTo("ADMIN");
        assertThat(rol.getPermisos()).isEqualTo(permisos);

        // @Value classes are immutable - test via new builder
        Rol rol2 = Rol.builder()
                .id((short) 1)
                .nombre("NEW_ADMIN")
                .permisos(permisos)
                .build();
        assertThat(rol2.getNombre()).isEqualTo("NEW_ADMIN");

        assertThat(rol.toString()).isNotBlank();
        assertThat(rol.hashCode()).isNotZero();
        assertThat(rol).isEqualTo(rol);
        assertThat(rol).isNotEqualTo(new Object());
        assertThat(rol).isNotEqualTo(rol2);
    }
}
