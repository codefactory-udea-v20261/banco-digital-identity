package com.udea.bancodigital.auth.infrastructure.config;

import com.udea.bancodigital.auth.domain.model.Usuario;
import com.udea.bancodigital.auth.domain.port.out.JwtProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtProvider implements JwtProviderPort {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration-ms}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public String generateToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", usuario.getRoles().stream()
                .map(rol -> rol.getNombre().toUpperCase())
                .collect(Collectors.toList()));
        claims.put("uid", usuario.getId().toString());
        claims.put("activo", usuario.isActivo());
        claims.put("bloqueado", usuario.isBloqueado());
        claims.put("mfaActivo", usuario.isMfaActivo());
        if (usuario.getClienteId() != null) {
            claims.put("clienteId", usuario.getClienteId().toString());
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(usuario.getCorreo())
                .id(UUID.randomUUID().toString()) // jti
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public long getExpirationTime() {
        return jwtExpirationMs;
    }

    @Override
    public String extractJti(String token) {
        Claims claims = getClaims(token);
        return claims.getId();
    }

    @Override
    public UUID extractUserId(String token) {
        Object userId = getClaims(token).get("uid");
        return UUID.fromString(userId.toString());
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    /*HU 06-Se crea un metodo para extraer la id del cliente*/
    public UUID extractClienteId(String token) {
    Object clienteId = getClaims(token).get("clienteId");
    if (clienteId == null) {
        return null;
    }
    return UUID.fromString(clienteId.toString());
}
}
