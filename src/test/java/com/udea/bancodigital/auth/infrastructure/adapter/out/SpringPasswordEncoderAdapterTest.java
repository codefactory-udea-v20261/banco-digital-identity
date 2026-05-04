package com.udea.bancodigital.auth.infrastructure.adapter.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringPasswordEncoderAdapterTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SpringPasswordEncoderAdapter adapter;

    @Test
    @DisplayName("Debe codificar contraseña")
    void encode_ShouldDelegateToEncoder() {
        when(passwordEncoder.encode("raw")).thenReturn("encoded");

        assertThat(adapter.encode("raw")).isEqualTo("encoded");
    }

    @Test
    @DisplayName("Debe verificar contraseña correcta")
    void matches_ShouldReturnTrueForMatch() {
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        assertThat(adapter.matches("raw", "encoded")).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar contraseña incorrecta")
    void matches_ShouldReturnFalseForMismatch() {
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThat(adapter.matches("wrong", "encoded")).isFalse();
    }
}
