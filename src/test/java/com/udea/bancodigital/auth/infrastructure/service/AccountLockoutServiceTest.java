package com.udea.bancodigital.auth.infrastructure.service;

import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountLockoutServiceTest {

    @Mock
    private UsuarioJpaRepository usuarioRepository;

    @InjectMocks
    private AccountLockoutService service;

    @Test
    @DisplayName("Debe registrar intento fallido e incrementar contador")
    void recordFailedAttempt_ShouldIncrementCounter() {
        ReflectionTestUtils.setField(service, "maxFailedAttempts", 5);
        UsuarioEntity usuario = UsuarioEntity.builder().failedAttempts(2).build();

        service.recordFailedAttempt(usuario);

        assertThat(usuario.getFailedAttempts()).isEqualTo(3);
        assertThat(usuario.getLastFailedAt()).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Debe llamar setBloqueado(true) al alcanzar máximo de intentos")
    void recordFailedAttempt_ShouldSetBloqueadoOnMaxAttempts() {
        ReflectionTestUtils.setField(service, "maxFailedAttempts", 3);
        UsuarioEntity usuario = UsuarioEntity.builder().failedAttempts(2).build();

        service.recordFailedAttempt(usuario);

        // setBloqueado(true) is called but isBloqueado() checks bloqueadoHasta
        // Verify the field was set via reflection
        boolean rawBloqueado = (boolean) ReflectionTestUtils.getField(usuario, "bloqueado");
        assertThat(rawBloqueado).isTrue();
        assertThat(usuario.getFailedAttempts()).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe manejar failedAttempts null")
    void recordFailedAttempt_ShouldHandleNullFailedAttempts() {
        ReflectionTestUtils.setField(service, "maxFailedAttempts", 5);
        UsuarioEntity usuario = UsuarioEntity.builder().failedAttempts(null).build();

        service.recordFailedAttempt(usuario);

        assertThat(usuario.getFailedAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe reiniciar intentos fallidos")
    void resetFailedAttempts_ShouldResetCounter() {
        UsuarioEntity usuario = UsuarioEntity.builder().failedAttempts(3).lastFailedAt(LocalDateTime.now()).build();

        service.resetFailedAttempts(usuario);

        assertThat(usuario.getFailedAttempts()).isEqualTo(0);
        assertThat(usuario.getLastFailedAt()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Debe retornar false cuando isBloqueado() es false")
    void isAccountLocked_ShouldReturnFalseWhenNotBlocked() {
        ReflectionTestUtils.setField(service, "lockoutDurationMinutes", 30);
        // isBloqueado() checks bloqueadoHasta - null means not blocked
        UsuarioEntity usuario = UsuarioEntity.builder().build();

        assertThat(service.isAccountLocked(usuario)).isFalse();
    }

    @Test
    @DisplayName("Debe retornar true cuando bloqueadoHasta es futuro y lockout no expirado")
    void isAccountLocked_ShouldReturnTrueWhenLockedAndNotExpired() {
        ReflectionTestUtils.setField(service, "lockoutDurationMinutes", 30);
        // isBloqueado() returns true when bloqueadoHasta is in the future
        UsuarioEntity usuario = UsuarioEntity.builder()
                .bloqueadoHasta(OffsetDateTime.now().plusDays(1))
                .lastFailedAt(LocalDateTime.now())
                .build();

        assertThat(service.isAccountLocked(usuario)).isTrue();
    }

    @Test
    @DisplayName("Debe auto-desbloquear cuando el bloqueo ha expirado")
    void isAccountLocked_ShouldAutoUnlockWhenExpired() {
        ReflectionTestUtils.setField(service, "lockoutDurationMinutes", 30);
        // bloqueadoHasta in future makes isBloqueado() true initially
        // but lastFailedAt 60 min ago means lock has expired
        UsuarioEntity usuario = UsuarioEntity.builder()
                .bloqueadoHasta(OffsetDateTime.now().plusDays(1))
                .failedAttempts(5)
                .lastFailedAt(LocalDateTime.now().minusMinutes(60))
                .build();

        assertThat(service.isAccountLocked(usuario)).isFalse();
    }

    @Test
    @DisplayName("Debe desbloquear cuenta explícitamente")
    void unlockAccount_ShouldResetAllLockFields() {
        UsuarioEntity usuario = UsuarioEntity.builder()
                .bloqueadoHasta(OffsetDateTime.now().plusDays(1))
                .failedAttempts(5).lastFailedAt(LocalDateTime.now()).build();

        service.unlockAccount(usuario);

        assertThat(usuario.getFailedAttempts()).isEqualTo(0);
        assertThat(usuario.getLastFailedAt()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Debe retornar 0 minutos cuando cuenta no está bloqueada")
    void getRemainingLockoutMinutes_ShouldReturnZeroWhenNotLocked() {
        UsuarioEntity usuario = UsuarioEntity.builder().build();

        assertThat(service.getRemainingLockoutMinutes(usuario)).isEqualTo(0);
    }

    @Test
    @DisplayName("Debe retornar minutos restantes de bloqueo")
    void getRemainingLockoutMinutes_ShouldReturnPositiveValue() {
        ReflectionTestUtils.setField(service, "lockoutDurationMinutes", 30);
        UsuarioEntity usuario = UsuarioEntity.builder()
                .bloqueadoHasta(OffsetDateTime.now().plusDays(1))
                .lastFailedAt(LocalDateTime.now().minusMinutes(10))
                .build();

        long remaining = service.getRemainingLockoutMinutes(usuario);
        assertThat(remaining).isBetween(19L, 21L);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando lastFailedAt es null")
    void getRemainingLockoutMinutes_ShouldReturnZeroWhenNoLastFailed() {
        UsuarioEntity usuario = UsuarioEntity.builder()
                .bloqueadoHasta(OffsetDateTime.now().plusDays(1))
                .lastFailedAt(null).build();

        assertThat(service.getRemainingLockoutMinutes(usuario)).isEqualTo(0);
    }
}
