package com.udea.bancodigital.auth.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    private UUID id;

    @Column(name = "cliente_id")
    private UUID clienteId;

    @Column(name = "username", nullable = false, unique = true)
    private String correo;

    @Column(name = "password_hash", nullable = false)
    private String clave;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "intentos_fallidos")
    @Builder.Default
    private Short intentosFallidos = 0;

    @Column(name = "mfa_secret")
    private String secretoMfa;

    @Column(name = "mfa_activo", nullable = false)
    @Builder.Default
    private boolean mfaActivo = false;

    @Column(name = "bloqueado_hasta")
    private OffsetDateTime bloqueadoHasta;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    private Set<RolEntity> roles = new HashSet<>();

    public boolean isBloqueado() {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(OffsetDateTime.now());
    }
}
