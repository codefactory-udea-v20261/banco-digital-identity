package com.udea.bancodigital.shared.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuditableEntity")
class AuditableEntityTest {
    private TestAuditableEntity entity;

    @BeforeEach
    void setUp() {
        entity = new TestAuditableEntity();
    }

    @Nested
    @DisplayName("prePersist()")
    class PrePersistTest {

        @Test
        @DisplayName("Debe establecer createdAt cuando es null")
        void debeEstablecerCreatedAtCuandoEsNull() {
            // Arrange
            entity.setCreatedAt(null);

            // Act
            entity.prePersist();

            // Assert
            assertThat(entity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Debe establecer updatedAt cuando es null")
        void debeEstablecerUpdatedAtCuandoEsNull() {
            // Arrange
            entity.setUpdatedAt(null);

            // Act
            entity.prePersist();

            // Assert
            assertThat(entity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Debe asignar SYSTEM como createdBy cuando es null")
        void debeAsignarSystemCuandoCreatedByEsNull() {
            // Arrange
            entity.setCreatedBy(null);

            // Act
            entity.prePersist();

            // Assert
            assertThat(entity.getCreatedBy()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("Debe asignar SYSTEM como createdBy cuando está en blanco")
        void debeAsignarSystemCuandoCreatedByEstaEnBlanco() {
            // Arrange
            entity.setCreatedBy("  ");

            // Act
            entity.prePersist();

            // Assert
            assertThat(entity.getCreatedBy()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("Debe preservar createdBy cuando ya tiene valor")
        void debePreservarCreatedByCuandoYaTieneValor() {
            // Arrange
            entity.setCreatedBy("usuario1");

            // Act
            entity.prePersist();

            // Assert
            assertThat(entity.getCreatedBy()).isEqualTo("usuario1");
        }

        @Test
        @DisplayName("Debe asignar updatedBy igual a createdBy en el primer persist")
        void debeAsignarUpdatedByIgualACreatedBy() {
            // Arrange
            entity.setCreatedBy("usuario1");

            // Act
            entity.prePersist();

            // Assert
            assertThat(entity.getUpdatedBy()).isEqualTo("usuario1");
        }

        @Test
        @DisplayName("No debe sobrescribir createdAt si ya tiene valor")
        void noDebeSobrescribirCreatedAtSiYaTieneValor() {
            // Arrange
            Instant original = Instant.now().minusSeconds(1000);
            entity.setCreatedAt(original);

            // Act
            entity.prePersist();

            // Assert
            assertThat(entity.getCreatedAt()).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("preUpdate()")
    class PreUpdateTest {

        @Test
        @DisplayName("Debe actualizar updatedAt en cada update")
        void debeActualizarUpdatedAt() {
            // Arrange
            Instant antigua = Instant.now().minusSeconds(100);
            entity.setUpdatedAt(antigua);

            // Act
            entity.preUpdate();

            // Assert
            assertThat(entity.getUpdatedAt()).isAfter(antigua);
        }

        @Test
        @DisplayName("Debe asignar SYSTEM como updatedBy cuando está en blanco")
        void debeAsignarSystemCuandoUpdatedByEstaEnBlanco() {
            // Arrange
            entity.setUpdatedBy("");

            // Act
            entity.preUpdate();

            // Assert
            assertThat(entity.getUpdatedBy()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("Debe preservar updatedBy cuando ya tiene valor")
        void debePreservarUpdatedByCuandoYaTieneValor() {
            // Arrange
            entity.setUpdatedBy("admin");

            // Act
            entity.preUpdate();

            // Assert
            assertThat(entity.getUpdatedBy()).isEqualTo("admin");
        }

        @Test
        @DisplayName("No debe modificar createdAt en el update")
        void noDebeModificarCreatedAt() {
            // Arrange
            Instant original = Instant.now().minusSeconds(1000);
            entity.setCreatedAt(original);

            // Act
            entity.preUpdate();

            // Assert
            assertThat(entity.getCreatedAt()).isEqualTo(original);
        }
    }

    /**
     * Clase concreta mínima para poder instanciar la clase abstracta
     * AuditableEntity.
     * No tiene anotaciones JPA para evitar dependencias del contexto de Spring.
     */
    static class TestAuditableEntity extends AuditableEntity {
    }
}