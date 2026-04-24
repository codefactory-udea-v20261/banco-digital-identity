package com.udea.bancodigital.shared.util;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Clase base para todas las entidades JPA del proyecto.
 * Proporciona los campos de auditoría automáticamente via Spring Data Auditing.
 * <p>
 * IMPORTANTE: Para activar el llenado automático de createdBy/updatedBy,
 * se debe implementar AuditorAware<String> cuando haya un usuario autenticado.
 * <p>
 * Uso:
 *   @Entity
 *   public class ClienteEntity extends AuditableEntity { ... }
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    private static final String DEFAULT_AUDITOR = "SYSTEM";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @PrePersist
    protected void prePersist() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (createdBy == null || createdBy.isBlank()) {
            createdBy = DEFAULT_AUDITOR;
        }
        if (updatedBy == null || updatedBy.isBlank()) {
            updatedBy = createdBy;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
        if (updatedBy == null || updatedBy.isBlank()) {
            updatedBy = DEFAULT_AUDITOR;
        }
    }
}
