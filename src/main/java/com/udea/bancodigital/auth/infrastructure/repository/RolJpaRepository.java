package com.udea.bancodigital.auth.infrastructure.repository;

import com.udea.bancodigital.auth.infrastructure.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolJpaRepository extends JpaRepository<RolEntity, Short> {
}
