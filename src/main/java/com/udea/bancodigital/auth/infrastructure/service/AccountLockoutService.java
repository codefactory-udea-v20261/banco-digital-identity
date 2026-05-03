package com.udea.bancodigital.auth.infrastructure.service;

import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountLockoutService {

    private final UsuarioJpaRepository usuarioRepository;

    @Value("${app.security.account-lockout.max-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.account-lockout.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    /**
     * Records a failed login attempt for a user
     */
    public void recordFailedAttempt(UsuarioEntity usuario) {
        usuario.setFailedAttempts((usuario.getFailedAttempts() != null ? usuario.getFailedAttempts() : 0) + 1);
        usuario.setLastFailedAt(LocalDateTime.now());

        if (usuario.getFailedAttempts() >= maxFailedAttempts) {
            usuario.setBloqueado(true);
        }

        usuarioRepository.save(usuario);
    }

    /**
     * Resets failed attempts after successful login
     */
    public void resetFailedAttempts(UsuarioEntity usuario) {
        usuario.setFailedAttempts(0);
        usuario.setLastFailedAt(null);
        usuarioRepository.save(usuario);
    }

    /**
     * Checks if account is locked and not expired
     */
    public boolean isAccountLocked(UsuarioEntity usuario) {
        if (!usuario.isBloqueado()) {
            return false;
        }

        // Check if lock has expired
        if (usuario.getLastFailedAt() != null) {
            LocalDateTime lockExpiry = usuario.getLastFailedAt().plusMinutes(lockoutDurationMinutes);
            if (LocalDateTime.now().isAfter(lockExpiry)) {
                // Auto-unlock after duration expires
                unlockAccount(usuario);
                return false;
            }
        }

        return true;
    }

    /**
     * Unlocks a previously locked account
     */
    public void unlockAccount(UsuarioEntity usuario) {
        usuario.setBloqueado(false);
        usuario.setFailedAttempts(0);
        usuario.setLastFailedAt(null);
        usuarioRepository.save(usuario);
    }

    /**
     * Gets remaining minutes until account is automatically unlocked
     */
    public long getRemainingLockoutMinutes(UsuarioEntity usuario) {
        if (!usuario.isBloqueado() || usuario.getLastFailedAt() == null) {
            return 0;
        }

        LocalDateTime lockExpiry = usuario.getLastFailedAt().plusMinutes(lockoutDurationMinutes);
        long minutesDifference = java.time.temporal.ChronoUnit.MINUTES.between(LocalDateTime.now(), lockExpiry);
        return Math.max(0, minutesDifference);
    }
}
