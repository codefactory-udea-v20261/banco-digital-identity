package com.udea.bancodigital.auth.infrastructure.repository;

import com.udea.bancodigital.auth.infrastructure.entity.PasswordHistorialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordHistorialJpaRepository extends JpaRepository<PasswordHistorialEntity, Long> {

    List<PasswordHistorialEntity> findByUsuarioIdOrderByCreatedAtDesc(UUID usuarioId);
}
