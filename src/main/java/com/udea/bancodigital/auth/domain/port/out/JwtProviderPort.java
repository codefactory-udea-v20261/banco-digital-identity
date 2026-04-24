package com.udea.bancodigital.auth.domain.port.out;

import com.udea.bancodigital.auth.domain.model.Usuario;

import java.util.Date;

/**
 * Puerto de salida para generación y validación de tokens JWT.
 * 
 * RESPONSABILIDAD:
 * - Generar tokens JWT
 * - Validar tokens
 * - Extraer información de tokens
 * 
 * La implementación concreta está en infrastructure y usa JJWT.
 */
public interface JwtProviderPort {
    
    /**
     * Genera un token JWT para un usuario.
     *
     * @param usuario Usuario para el cual generar el token
     * @return Token JWT generado
     */
    String generateToken(Usuario usuario);
    
    /**
     * Valida si un token es válido.
     *
     * @param token Token a validar
     * @return true si es válido, false en caso contrario
     */
    boolean isTokenValid(String token);
    
    /**
     * Extrae el username (email) del token.
     *
     * @param token Token JWT
     * @return Username (email) contenido en el token
     */
    String extractUsername(String token);
    
    /**
     * Extrae la fecha de expiración del token.
     *
     * @param token Token JWT
     * @return Fecha de expiración
     */
    Date extractExpiration(String token);
    
    /**
     * Extrae el JTI (JWT ID) del token.
     *
     * @param token Token JWT
     * @return JTI del token
     */
    String extractJti(String token);

    /**
     * Extrae el identificador del usuario autenticado.
     *
     * @param token Token JWT
     * @return UUID del usuario autenticado
     */
    java.util.UUID extractUserId(String token);
    
    /**
     * Obtiene el tiempo de expiración configurado en milisegundos.
     *
     * @return Tiempo de expiración en milisegundos
     */
    long getExpirationTime();
}
