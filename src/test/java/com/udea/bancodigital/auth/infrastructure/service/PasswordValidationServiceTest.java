package com.udea.bancodigital.auth.infrastructure.service;

import com.udea.bancodigital.auth.application.dto.PasswordValidationResultDto;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordValidationServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioJpaRepository usuarioRepository;

    @InjectMocks
    private PasswordValidationService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "minLength", 8);
        ReflectionTestUtils.setField(service, "requireUppercase", true);
        ReflectionTestUtils.setField(service, "requireLowercase", true);
        ReflectionTestUtils.setField(service, "requireNumbers", true);
        ReflectionTestUtils.setField(service, "requireSpecialChars", true);
        ReflectionTestUtils.setField(service, "preventReuse", true);
    }

    @Test
    @DisplayName("Debe validar contraseña fuerte como válida")
    void validate_StrongPassword_ShouldBeValid() {
        PasswordValidationResultDto result = service.validate("Test1234!");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getStrengthScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("Debe rechazar contraseña corta")
    void validate_ShortPassword_ShouldFail() {
        PasswordValidationResultDto result = service.validate("Te1!");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("8 caracteres"));
    }

    @Test
    @DisplayName("Debe rechazar contraseña vacía")
    void validate_EmptyPassword_ShouldFail() {
        PasswordValidationResultDto result = service.validate("");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar contraseña sin mayúscula")
    void validate_NoUppercase_ShouldFail() {
        PasswordValidationResultDto result = service.validate("test1234!");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("mayúscula"));
    }

    @Test
    @DisplayName("Debe rechazar contraseña sin minúscula")
    void validate_NoLowercase_ShouldFail() {
        PasswordValidationResultDto result = service.validate("TEST1234!");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("minúscula"));
    }

    @Test
    @DisplayName("Debe rechazar contraseña sin número")
    void validate_NoNumbers_ShouldFail() {
        PasswordValidationResultDto result = service.validate("TestTest!");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("número"));
    }

    @Test
    @DisplayName("Debe rechazar contraseña sin carácter especial")
    void validate_NoSpecialChars_ShouldFail() {
        PasswordValidationResultDto result = service.validate("Test1234");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("especial"));
    }

    @Test
    @DisplayName("Debe validar contraseña actual correcta")
    void validateOldPassword_ShouldReturnTrue() {
        when(passwordEncoder.matches("old", "encoded")).thenReturn(true);

        assertThat(service.validateOldPassword("old", "encoded")).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar contraseña actual incorrecta")
    void validateOldPassword_ShouldReturnFalse() {
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThat(service.validateOldPassword("wrong", "encoded")).isFalse();
    }

    @Test
    @DisplayName("Debe verificar que contraseñas coincidan")
    void passwordsMatch_ShouldReturnTrue() {
        assertThat(service.passwordsMatch("abc", "abc")).isTrue();
    }

    @Test
    @DisplayName("Debe detectar contraseñas que no coinciden")
    void passwordsMatch_ShouldReturnFalse() {
        assertThat(service.passwordsMatch("abc", "xyz")).isFalse();
    }

    @Test
    @DisplayName("Debe manejar password null en passwordsMatch")
    void passwordsMatch_NullPassword_ShouldReturnFalse() {
        assertThat(service.passwordsMatch(null, "abc")).isFalse();
    }

    @Test
    @DisplayName("Debe detectar reutilización de contraseña")
    void isPasswordReused_ShouldReturnTrueWhenSame() {
        UsuarioEntity entity = UsuarioEntity.builder().clave("encoded").build();
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);

        assertThat(service.isPasswordReused("password", Optional.of(entity))).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false cuando usuario no existe")
    void isPasswordReused_ShouldReturnFalseWhenEmpty() {
        assertThat(service.isPasswordReused("password", Optional.empty())).isFalse();
    }

    @Test
    @DisplayName("Debe codificar contraseña")
    void encodePassword_ShouldDelegate() {
        when(passwordEncoder.encode("raw")).thenReturn("encoded");

        assertThat(service.encodePassword("raw")).isEqualTo("encoded");
    }

    @Test
    @DisplayName("Debe retornar descripciones de fortaleza")
    void getStrengthDescription_ShouldReturnCorrectLabels() {
        assertThat(service.getStrengthDescription(90)).isEqualTo("Muy Fuerte");
        assertThat(service.getStrengthDescription(70)).isEqualTo("Fuerte");
        assertThat(service.getStrengthDescription(50)).isEqualTo("Moderada");
        assertThat(service.getStrengthDescription(30)).isEqualTo("Débil");
        assertThat(service.getStrengthDescription(10)).isEqualTo("Muy Débil");
    }
}
