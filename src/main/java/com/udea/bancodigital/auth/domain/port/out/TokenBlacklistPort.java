package com.udea.bancodigital.auth.domain.port.out;

import java.time.Instant;
import java.util.UUID;

/**
 * Puerto de salida para gestión de tokens en blacklist.
 * 
 * RESPONSABILIDAD:
 * - Verificar si un token ha sido revocado (logout)
 * - Agregar tokens a la blacklist
 * 
 * IMPLEMENTACIONES POSIBLES:
 * - Redis (para producción)
 * - Base de datos
 * - Cache en memoria (solo desarrollo)
 */
public interface TokenBlacklistPort {
    
    /**
     * Verifica si un token está revocado.
     *
     * @param token El JTI del token JWT a verificar
     * @return true si está revocado, false si es válido
     */
    boolean isRevoked(String token);
    
    /**
     * Revoca un token agregándolo a la blacklist.
     *
     * @param token El JTI del token a revocar
     * @param usuarioId El usuario dueño del token
     * @param expirationTime Instante real de expiración del token
     */
    void revoke(String token, UUID usuarioId, Instant expirationTime);
}
