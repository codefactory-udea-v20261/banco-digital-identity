package com.udea.bancodigital.auth.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermisoTest {
    @Test
    void shouldBuildPermiso() {
        Permiso p = Permiso.builder().id((short) 1).nombre("READ").descripcion("Read access").build();
        assertThat(p.getId()).isEqualTo((short) 1);
        assertThat(p.getNombre()).isEqualTo("READ");
        assertThat(p.getDescripcion()).isEqualTo("Read access");
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        Permiso p1 = Permiso.builder().id((short) 1).nombre("READ").build();
        Permiso p2 = Permiso.builder().id((short) 1).nombre("READ").build();
        Permiso p3 = Permiso.builder().id((short) 2).nombre("WRITE").build();
        assertThat(p1).isEqualTo(p2);
        assertThat(p1).isNotEqualTo(p3);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        assertThat(p1.toString()).contains("READ");
    }
}
