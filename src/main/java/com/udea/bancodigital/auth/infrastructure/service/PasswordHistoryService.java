package com.udea.bancodigital.auth.infrastructure.service;

import com.udea.bancodigital.auth.infrastructure.entity.PasswordHistorialEntity;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.PasswordHistorialJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordHistoryService {

    private final PasswordHistorialJpaRepository passwordHistorialRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.password-policy.prevent-reuse:true}")
    private boolean preventReuse;

    @Value("${app.security.password-policy.password-history-count:3}")
    private int historyCount;

    public boolean isSameAsCurrent(String plainPassword, UsuarioEntity usuario) {
        if (!preventReuse || plainPassword == null) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, usuario.getClave());
    }

    public boolean isInHistory(String plainPassword, UUID usuarioId) {
        if (!preventReuse || plainPassword == null) {
            return false;
        }
        return passwordHistorialRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId).stream()
                .limit(historyCount)
                .anyMatch(entry -> passwordEncoder.matches(plainPassword, entry.getPasswordHash()));
    }

    @Transactional
    public void recordPreviousPassword(UUID usuarioId, String previousPasswordHash) {
        PasswordHistorialEntity entry = PasswordHistorialEntity.builder()
                .usuarioId(usuarioId)
                .passwordHash(previousPasswordHash)
                .build();
        passwordHistorialRepository.save(entry);

        List<PasswordHistorialEntity> entries =
                passwordHistorialRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);
        if (entries.size() > historyCount) {
            List<Long> idsToRemove = entries.subList(historyCount, entries.size()).stream()
                    .map(PasswordHistorialEntity::getId)
                    .toList();
            passwordHistorialRepository.deleteAllById(idsToRemove);
        }
    }
}
