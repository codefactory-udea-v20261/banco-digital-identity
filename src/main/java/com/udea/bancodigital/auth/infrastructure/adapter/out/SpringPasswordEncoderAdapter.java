package com.udea.bancodigital.auth.infrastructure.adapter.out;

import com.udea.bancodigital.auth.domain.port.out.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador de Spring Security PasswordEncoder al puerto del dominio.
 * 
 * Este adaptador permite que el dominio use la codificación de contraseñas
 * de Spring Security sin depender directamente de Spring.
 * 
 * PRINCIPIOS:
 * - Adapter Pattern: Adapta Spring Security al puerto del dominio
 * - Dependency Inversion: El dominio depende de PasswordEncoderPort (abstracción)
 */
@Component
@RequiredArgsConstructor
public class SpringPasswordEncoderAdapter implements PasswordEncoderPort {
    
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
