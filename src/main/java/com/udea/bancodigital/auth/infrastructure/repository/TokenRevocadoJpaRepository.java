package com.udea.bancodigital.auth.infrastructure.repository;

import com.udea.bancodigital.auth.infrastructure.entity.TokenRevocadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface TokenRevocadoJpaRepository extends JpaRepository<TokenRevocadoEntity, Long> {

    boolean existsByJtiAndExpiraAtAfter(String jti, OffsetDateTime now);

    Optional<TokenRevocadoEntity> findByJti(String jti);
}
