package com.udea.bancodigital.infrastructure.config;

import com.udea.bancodigital.auth.infrastructure.entity.RolEntity;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.RolJpaRepository;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final RolJpaRepository rolJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioJpaRepository.existsByCorreo("admin@bancodigital.com")) {
            log.info("Admin user already exists.");
            return;
        }

        RolEntity adminRol = rolJpaRepository.findById((short) 1)
                .orElseGet(() -> rolJpaRepository.save(RolEntity.builder().id((short) 1).nombre("ADMIN").build()));
        
        // Ensure other roles exist
        if (!rolJpaRepository.existsById((short) 2)) rolJpaRepository.save(RolEntity.builder().id((short) 2).nombre("CAJERO").build());
        if (!rolJpaRepository.existsById((short) 3)) rolJpaRepository.save(RolEntity.builder().id((short) 3).nombre("CLIENTE").build());
        if (!rolJpaRepository.existsById((short) 4)) rolJpaRepository.save(RolEntity.builder().id((short) 4).nombre("AUDITOR").build());

        UsuarioEntity admin = UsuarioEntity.builder()
                .id(UUID.randomUUID())
                .correo("admin@bancodigital.com")
                .clave(passwordEncoder.encode("Admin123!"))
                .roles(Collections.singleton(adminRol))
                .activo(true)
                .build();

        usuarioJpaRepository.save(admin);
        log.info("Admin user seeded successfully: admin@bancodigital.com / Admin123!");
    }
}
