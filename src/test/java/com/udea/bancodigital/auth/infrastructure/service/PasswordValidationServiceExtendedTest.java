package com.udea.bancodigital.auth.infrastructure.service;

import com.udea.bancodigital.auth.application.dto.PasswordValidationResultDto;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordValidationService - Extended")
class PasswordValidationServiceExtendedTest {
    @Mock
    private UsuarioJpaRepository usuarioRepository;

    private PasswordValidationService service;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new PasswordValidationService(passwordEncoder, usuarioRepository);
        ReflectionTestUtils.setField(service, "minLength", 8);
        ReflectionTestUtils.setField(service, "requireUppercase", true);
        ReflectionTestUtils.setField(service, "requireLowercase", true);
        ReflectionTestUtils.setField(service, "requireNumbers", true);
        ReflectionTestUtils.setField(service, "requireSpecialChars", true);
        ReflectionTestUtils.setField(service, "preventReuse", true);
    }

    @Nested
    @DisplayName("encodePassword()")
    class EncodePasswordTest {

        @Test
        @DisplayName("Debe retornar hash diferente al texto plano")
        void debeRetornarHashDiferenteAlTextoPlano() {
            // Arrange
            String password = "MiClave123!";

            // Act
            String encoded = service.encodePassword(password);

            // Assert
            assertThat(encoded).isNotEqualTo(password);
            assertThat(encoded).isNotBlank();
        }

        @Test
        @DisplayName("El hash debe ser verificable con el encoder")
        void elHashDebeSerVerificable() {
            // Arrange
            String password = "MiClave123!";

            // Act
            String encoded = service.encodePassword(password);

            // Assert
            assertThat(passwordEncoder.matches(password, encoded)).isTrue();
        }
    }

    @Nested
    @DisplayName("getStrengthDescription()")
    class GetStrengthDescriptionTest {

        @Test
        @DisplayName("Score >= 90 debe retornar 'Muy Fuerte'")
        void scoreMuyFuerte() {
            // Arrange / Act / Assert
            assertThat(service.getStrengthDescription(100)).isEqualTo("Muy Fuerte");
            assertThat(service.getStrengthDescription(90)).isEqualTo("Muy Fuerte");
        }

        @Test
        @DisplayName("Score >= 70 debe retornar 'Fuerte'")
        void scoreFuerte() {
            // Arrange / Act / Assert
            assertThat(service.getStrengthDescription(80)).isEqualTo("Fuerte");
            assertThat(service.getStrengthDescription(70)).isEqualTo("Fuerte");
        }

        @Test
        @DisplayName("Score >= 50 debe retornar 'Moderada'")
        void scoreModerada() {
            // Arrange / Act / Assert
            assertThat(service.getStrengthDescription(60)).isEqualTo("Moderada");
            assertThat(service.getStrengthDescription(50)).isEqualTo("Moderada");
        }

        @Test
        @DisplayName("Score >= 30 debe retornar 'Débil'")
        void scoreDebil() {
            // Arrange / Act / Assert
            assertThat(service.getStrengthDescription(40)).isEqualTo("Débil");
            assertThat(service.getStrengthDescription(30)).isEqualTo("Débil");
        }

        @Test
        @DisplayName("Score < 30 debe retornar 'Muy Débil'")
        void scoreMuyDebil() {
            // Arrange / Act / Assert
            assertThat(service.getStrengthDescription(20)).isEqualTo("Muy Débil");
            assertThat(service.getStrengthDescription(0)).isEqualTo("Muy Débil");
        }
    }

    @Nested
    @DisplayName("isPasswordReused()")
    class IsPasswordReusedTest {

        @Test
        @DisplayName("Debe retornar false cuando el Optional de usuario está vacío")
        void debeRetornarFalseCuandoUsuarioEstaVacio() {
            // Arrange / Act
            boolean reused = service.isPasswordReused("cualquierClave", Optional.empty());

            // Assert
            assertThat(reused).isFalse();
        }

        @Test
        @DisplayName("Debe retornar true cuando la clave es la misma que la actual")
        void debeRetornarTrueCuandoClaveIgual() {
            // Arrange
            String password = "MiClave123!";
            String encoded = passwordEncoder.encode(password);
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setClave(encoded);

            // Act
            boolean reused = service.isPasswordReused(password, Optional.of(usuario));

            // Assert
            assertThat(reused).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando la clave es diferente a la actual")
        void debeRetornarFalseCuandoClaveDiferente() {
            // Arrange
            String currentPassword = "ClaveActual123!";
            String encoded = passwordEncoder.encode(currentPassword);
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setClave(encoded);

            // Act
            boolean reused = service.isPasswordReused("NuevaClave456!", Optional.of(usuario));

            // Assert
            assertThat(reused).isFalse();
        }
    }

    @Nested
    @DisplayName("validate() - casos adicionales")
    class ValidateAdicionalesTest {

        @Test
        @DisplayName("Debe fallar cuando la clave es muy corta")
        void debeFallarCuandoClaveEsMuyCorta() {
            // Arrange / Act
            PasswordValidationResultDto result = service.validate("ab");

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).isNotEmpty();
        }

        @Test
        @DisplayName("El score debe ser 100 para una clave que cumple todos los requisitos")
        void elScoreDebeSerMaximoParaClaveFuerte() {
            // Arrange / Act
            PasswordValidationResultDto result = service.validate("MiClave123!");

            // Assert
            assertThat(result.isValid()).isTrue();
            assertThat(result.getStrengthScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("Debe acumular múltiples errores cuando faltan varios requisitos")
        void debeAcumularMultiplesErrores() {
            // Arrange / Act
            PasswordValidationResultDto result = service.validate("abc");

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSizeGreaterThan(1);
        }
    }

}
