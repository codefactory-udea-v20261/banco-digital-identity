package com.udea.bancodigital.auth.domain.port.out;

/**
 * Puerto de salida para codificación de contraseñas.
 * 
 * RESPONSABILIDAD:
 * - Codificar contraseñas en texto plano
 * - Verificar contraseñas contra hash
 * 
 * IMPLEMENTACIONES POSIBLES:
 * - BCrypt (recomendado)
 * - Argon2
 * - PBKDF2
 */
public interface PasswordEncoderPort {
    
    /**
     * Codifica una contraseña en texto plano.
     *
     * @param rawPassword Contraseña en texto plano
     * @return Contraseña codificada (hash)
     */
    String encode(String rawPassword);
    
    /**
     * Verifica si una contraseña en texto plano coincide con un hash.
     *
     * @param rawPassword Contraseña en texto plano
     * @param encodedPassword Contraseña codificada (hash)
     * @return true si coinciden, false en caso contrario
     */
    boolean matches(String rawPassword, String encodedPassword);
}
