package com.udea.bancodigital.auth.infrastructure.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RolEntityTest {
    @Test
    void shouldTestRolEntity() {
        PermisoEntity permiso = PermisoEntity.builder().id((short)1).nombre("READ").descripcion("Read access").build();
        assertThat(permiso.getId()).isEqualTo((short)1);
        assertThat(permiso.getNombre()).isEqualTo("READ");
        assertThat(permiso.getDescripcion()).isEqualTo("Read access");

        RolEntity rol = RolEntity.builder().id((short)1).nombre("ADMIN").build();
        rol.getPermisos().add(permiso);
        assertThat(rol.getId()).isEqualTo((short)1);
        assertThat(rol.getNombre()).isEqualTo("ADMIN");
        assertThat(rol.getPermisos()).hasSize(1);

        rol.setNombre("SUPER");
        assertThat(rol.getNombre()).isEqualTo("SUPER");

        permiso.setDescripcion("Write access");
        assertThat(permiso.getDescripcion()).isEqualTo("Write access");
    }
}
