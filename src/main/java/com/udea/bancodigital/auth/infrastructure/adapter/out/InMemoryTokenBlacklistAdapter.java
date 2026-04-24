package com.udea.bancodigital.auth.infrastructure.adapter.out;

import com.udea.bancodigital.auth.domain.port.out.TokenBlacklistPort;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del blacklist de tokens.
 * 
 * NOTA: Esta es una implementación básica para desarrollo.
 * En producción se debe usar Redis para persistencia distribuida.
 * 
 * LIMITACIONES:
 * - Los tokens se almacenan en memoria (se pierden al reiniciar)
 * - No funciona en ambientes multi-instancia
 * - No hay limpieza automática de tokens expirados
 * 
 * MEJORA FUTURA: Implementar con Redis usando Spring Data Redis
 */
@Slf4j
public class InMemoryTokenBlacklistAdapter implements TokenBlacklistPort {
    
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    
    @Override
    public boolean isRevoked(String token) {
        boolean revoked = blacklistedTokens.containsKey(token);
        
        if (revoked) {
            // Verificar si ya expiró y limpiar
            Long expirationTime = blacklistedTokens.get(token);
            if (System.currentTimeMillis() > expirationTime) {
                blacklistedTokens.remove(token);
                return false;
            }
        }
        
        return revoked;
    }
    
    @Override
    public void revoke(String token, UUID usuarioId, Instant expirationTime) {
        blacklistedTokens.put(token, expirationTime.toEpochMilli());
        log.info("Token agregado al blacklist. Total tokens revocados: {}", blacklistedTokens.size());
    }
    
    /**
     * Método de utilidad para limpiar tokens expirados.
     * En producción, esto se haría con un scheduled task.
     */
    public void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
        log.debug("Tokens expirados limpiados. Tokens activos en blacklist: {}", blacklistedTokens.size());
    }
}
