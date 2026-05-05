package com.udea.bancodigital.auth.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PermisoEntity")
public class PermisoEntityTest {
    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @Test
        @DisplayName("Debe construir entidad con todos los campos")
        void debeConstruirConTodosLosCampos() {
            // Arrange / Act
            PermisoEntity permiso = PermisoEntity.builder()
                    .id((short) 1)
                    .nombre("PERM_MANAGE_CLIENTS")
                    .descripcion("Permiso para gestionar clientes")
                    .build();

            // Assert
            assertThat(permiso.getId()).isEqualTo((short) 1);
            assertThat(permiso.getNombre()).isEqualTo("PERM_MANAGE_CLIENTS");
            assertThat(permiso.getDescripcion()).isEqualTo("Permiso para gestionar clientes");
        }

        @Test
        @DisplayName("Debe construir entidad sin descripción")
        void debeConstruirSinDescripcion() {
            // Arrange / Act
            PermisoEntity permiso = PermisoEntity.builder()
                    .id((short) 2)
                    .nombre("PERM_READ_OWN_PROFILE")
                    .build();

            // Assert
            assertThat(permiso.getNombre()).isEqualTo("PERM_READ_OWN_PROFILE");
            assertThat(permiso.getDescripcion()).isNull();
        }
    }

    @Nested
    @DisplayName("Setters")
    class SettersTest {

        @Test
        @DisplayName("Debe permitir modificar los campos via setters")
        void debePermitirModificarCampos() {
            // Arrange
            PermisoEntity permiso = new PermisoEntity();

            // Act
            permiso.setId((short) 3);
            permiso.setNombre("PERM_NEW");
            permiso.setDescripcion("Nuevo permiso");

            // Assert
            assertThat(permiso.getId()).isEqualTo((short) 3);
            assertThat(permiso.getNombre()).isEqualTo("PERM_NEW");
            assertThat(permiso.getDescripcion()).isEqualTo("Nuevo permiso");
        }
    }

    @Nested
    @DisplayName("Constructor con todos los argumentos")
    class AllArgsConstructorTest {

        @Test
        @DisplayName("Debe crear entidad con constructor completo")
        void debCrearConConstructorCompleto() {
            // Arrange / Act
            PermisoEntity permiso = new PermisoEntity(
                    (short) 4,
                    "PERM_MANAGE_ACCOUNTS",
                    "Permiso para gestionar cuentas");

            // Assert
            assertThat(permiso.getId()).isEqualTo((short) 4);
            assertThat(permiso.getNombre()).isEqualTo("PERM_MANAGE_ACCOUNTS");
            assertThat(permiso.getDescripcion()).isEqualTo("Permiso para gestionar cuentas");
        }
    }

}
