package com.udea.bancodigital.auth.infrastructure.repository;

import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para UsuarioEntity.
 */
@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {
    
    Optional<UsuarioEntity> findByCorreo(String correo);
    
    boolean existsByCorreo(String correo);
}
