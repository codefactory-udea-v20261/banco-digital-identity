package com.udea.bancodigital.auth.infrastructure.service;

import com.udea.bancodigital.auth.infrastructure.entity.PasswordHistorialEntity;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.PasswordHistorialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordHistoryServiceTest {

    @Mock
    private PasswordHistorialJpaRepository passwordHistorialRepository;

    @InjectMocks
    private PasswordHistoryService passwordHistoryService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordHistoryService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(passwordHistoryService, "preventReuse", true);
        ReflectionTestUtils.setField(passwordHistoryService, "historyCount", 3);
    }

    @Test
    @DisplayName("Debe detectar contraseña en el historial")
    void isInHistory_whenPasswordWasUsed_returnsTrue() {
        UUID userId = UUID.randomUUID();
        String oldPassword = "ClaveAntigua123!";
        String hash = passwordEncoder.encode(oldPassword);
        when(passwordHistorialRepository.findByUsuarioIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(PasswordHistorialEntity.builder().passwordHash(hash).build()));

        assertThat(passwordHistoryService.isInHistory(oldPassword, userId)).isTrue();
    }

    @Test
    @DisplayName("Debe registrar hash anterior al cambiar contraseña")
    void recordPreviousPassword_savesEntry() {
        UUID userId = UUID.randomUUID();
        when(passwordHistorialRepository.findByUsuarioIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        passwordHistoryService.recordPreviousPassword(userId, "hash-anterior");

        verify(passwordHistorialRepository).save(any(PasswordHistorialEntity.class));
    }

    @Test
    @DisplayName("Debe detectar contraseña igual a la actual")
    void isSameAsCurrent_whenMatches_returnsTrue() {
        String password = "MiClave123!";
        UsuarioEntity usuario = UsuarioEntity.builder()
                .clave(passwordEncoder.encode(password))
                .build();

        assertThat(passwordHistoryService.isSameAsCurrent(password, usuario)).isTrue();
    }
}
