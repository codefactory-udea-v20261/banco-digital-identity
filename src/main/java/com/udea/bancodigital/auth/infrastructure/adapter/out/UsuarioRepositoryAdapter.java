package com.udea.bancodigital.auth.infrastructure.adapter.out;

import com.udea.bancodigital.auth.domain.model.Rol;
import com.udea.bancodigital.auth.domain.model.Usuario;
import com.udea.bancodigital.auth.domain.port.out.UsuarioRepositoryPort;
import com.udea.bancodigital.auth.infrastructure.entity.RolEntity;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.RolJpaRepository;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador JPA para el repositorio de usuarios.
 * 
 * Implementa el puerto del dominio usando Spring Data JPA.
 * Realiza la conversión entre entidades JPA y modelos de dominio.
 */
@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    @Value("${app.security.login.lockout-minutes:15}")
    private long lockoutMinutes = 15;
    
    private final UsuarioJpaRepository jpaRepository;
    private final RolJpaRepository rolJpaRepository;
    
    @Override
    public Optional<Usuario> findByUsername(String username) {
        // En este sistema, el username es el correo
        return findByEmail(username);
    }
    
    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByCorreo(email)
                .map(this::toDomain);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByCorreo(username);
    }
    
    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = toEntity(usuario);
        UsuarioEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // Métodos de mapeo
    // ═══════════════════════════════════════════════════════════════════════
    
    private Usuario toDomain(UsuarioEntity entity) {
        return Usuario.builder()
                .id(entity.getId())
                .clienteId(entity.getClienteId())
                .correo(entity.getCorreo())
                .clave(entity.getClave())
                .activo(entity.isActivo())
                .bloqueado(entity.isBloqueado())
                .intentosFallidos(entity.getIntentosFallidos() == null ? null : (int) entity.getIntentosFallidos())
                .secretoMfa(entity.getSecretoMfa())
                .mfaActivo(entity.isMfaActivo())
                .roles(mapRolesToDomain(entity.getRoles()))
                .build();
    }
    
    private UsuarioEntity toEntity(Usuario usuario) {
        return UsuarioEntity.builder()
                .id(usuario.getId())
                .clienteId(usuario.getClienteId())
                .correo(usuario.getCorreo())
                .clave(usuario.getClave())
                .activo(usuario.isActivo())
                .intentosFallidos(usuario.getIntentosFallidos() == null ? null : usuario.getIntentosFallidos().shortValue())
                .secretoMfa(usuario.getSecretoMfa())
                .mfaActivo(usuario.isMfaActivo())
                .bloqueadoHasta(usuario.isBloqueado() ? OffsetDateTime.now().plusMinutes(lockoutMinutes) : null)
                .roles(mapRolesToEntity(usuario.getRoles()))
                .build();
    }
    
    private Set<Rol> mapRolesToDomain(Set<RolEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Set.of();
        }
        return entities.stream()
                .map(entity -> Rol.builder()
                        .id(entity.getId())
                        .nombre(entity.getNombre())
                        .build())
                .collect(Collectors.toSet());
    }
    
    private Set<RolEntity> mapRolesToEntity(Set<Rol> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        List<Short> roleIds = roles.stream()
                .map(Rol::getId)
                .toList();
        Set<RolEntity> entities = rolJpaRepository.findAllById(roleIds).stream()
                .collect(Collectors.toSet());
        if (entities.size() != roleIds.size()) {
            throw new IllegalArgumentException("Uno o mas roles no existen en base de datos");
        }
        return entities;
    }
}
