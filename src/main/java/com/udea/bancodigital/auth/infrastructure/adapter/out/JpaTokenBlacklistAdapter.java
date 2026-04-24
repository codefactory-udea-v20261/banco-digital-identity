package com.udea.bancodigital.auth.infrastructure.adapter.out;

import com.udea.bancodigital.auth.domain.port.out.TokenBlacklistPort;
import com.udea.bancodigital.auth.infrastructure.entity.TokenRevocadoEntity;
import com.udea.bancodigital.auth.infrastructure.repository.TokenRevocadoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaTokenBlacklistAdapter implements TokenBlacklistPort {

    private final TokenRevocadoJpaRepository tokenRevocadoJpaRepository;

    @Override
    public boolean isRevoked(String token) {
        return tokenRevocadoJpaRepository.existsByJtiAndExpiraAtAfter(token, OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public void revoke(String token, UUID usuarioId, Instant expirationTime) {
        if (tokenRevocadoJpaRepository.findByJti(token).isPresent()) {
            return;
        }

        tokenRevocadoJpaRepository.save(TokenRevocadoEntity.builder()
                .jti(token)
                .usuarioId(usuarioId)
                .revocadoAt(OffsetDateTime.now(ZoneOffset.UTC))
                .expiraAt(OffsetDateTime.ofInstant(expirationTime, ZoneOffset.UTC))
                .build());
    }
}
