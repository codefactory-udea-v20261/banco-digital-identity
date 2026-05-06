package com.udea.bancodigital.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Excepciones de dominio - Identity")
class SharedExceptionsTest {
    @Nested
    @DisplayName("ClienteYaExisteException")
    class ClienteYaExisteExceptionTest {

        @Test
        @DisplayName("Debe incluir campo y valor en el mensaje con status CONFLICT")
        void debeIncluirCampoYValor() {
            // Arrange
            String campo = "email";
            String valor = "juan@banco.com";

            // Act
            ClienteYaExisteException ex = new ClienteYaExisteException(campo, valor);

            // Assert
            assertThat(ex.getErrorCode()).isEqualTo("CLIENTE_YA_EXISTE");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(ex.getMessage()).contains(campo).contains(valor);
        }
    }

    @Nested
    @DisplayName("CredencialesInvalidasException")
    class CredencialesInvalidasExceptionTest {

        @Test
        @DisplayName("Debe tener código AUTH_001 y status UNAUTHORIZED")
        void debeTenerCodigoYStatus() {
            // Arrange / Act
            CredencialesInvalidasException ex = new CredencialesInvalidasException();

            // Assert
            assertThat(ex.getErrorCode()).isEqualTo("AUTH_001");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getMessage()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("CuentaBloqueadaException")
    class CuentaBloqueadaExceptionTest {

        @Test
        @DisplayName("Debe tener código AUTH_002 y status FORBIDDEN")
        void debeTenerCodigoYStatus() {
            // Arrange / Act
            CuentaBloqueadaException ex = new CuentaBloqueadaException();

            // Assert
            assertThat(ex.getErrorCode()).isEqualTo("AUTH_002");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(ex.getMessage()).containsIgnoringCase("bloqueada");
        }
    }

    @Nested
    @DisplayName("DatosIncompletosException")
    class DatosIncompletosExceptionTest {

        @Test
        @DisplayName("Debe incluir el mensaje recibido y tener status BAD_REQUEST")
        void debeIncluirMensaje() {
            // Arrange
            String mensaje = "El campo email es obligatorio";

            // Act
            DatosIncompletosException ex = new DatosIncompletosException(mensaje);

            // Assert
            assertThat(ex.getErrorCode()).isEqualTo("DATOS_INCOMPLETOS");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getMessage()).isEqualTo(mensaje);
        }
    }

    @Nested
    @DisplayName("InvalidPasswordException")
    class InvalidPasswordExceptionTest {

        @Test
        @DisplayName("Debe incluir el mensaje recibido y tener status BAD_REQUEST")
        void debeIncluirMensaje() {
            // Arrange
            String mensaje = "La contraseña no cumple los requisitos";

            // Act
            InvalidPasswordException ex = new InvalidPasswordException(mensaje);

            // Assert
            assertThat(ex.getErrorCode()).isEqualTo("INVALID_PASSWORD");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getMessage()).isEqualTo(mensaje);
        }
    }

    @Nested
    @DisplayName("MfaRequeridoException")
    class MfaRequeridoExceptionTest {

        @Test
        @DisplayName("Debe tener código AUTH_003 y status UNAUTHORIZED")
        void debeTenerCodigoYStatus() {
            // Arrange / Act
            MfaRequeridoException ex = new MfaRequeridoException();

            // Assert
            assertThat(ex.getErrorCode()).isEqualTo("AUTH_003");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getMessage()).containsIgnoringCase("MFA");
        }
    }

    @Nested
    @DisplayName("PasswordChangeException")
    class PasswordChangeExceptionTest {

        @Test
        @DisplayName("Debe incluir el mensaje recibido y tener status BAD_REQUEST")
        void debeIncluirMensaje() {
            // Arrange
            String mensaje = "La contraseña actual no es correcta";

            // Act
            PasswordChangeException ex = new PasswordChangeException(mensaje);

            // Assert
            assertThat(ex.getErrorCode()).isEqualTo("PASSWORD_CHANGE_ERROR");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getMessage()).isEqualTo(mensaje);
        }
    }

    @Nested
    @DisplayName("UsuarioNoEncontradoException")
    class UsuarioNoEncontradoExceptionTest {

        @Test
        @DisplayName("Debe tener código USUARIO_NO_ENCONTRADO y status NOT_FOUND")
        void debeTenerCodigoYStatus() {
            // Arrange / Act
            UsuarioNoEncontradoException ex = new UsuarioNoEncontradoException();

            // Assert
            assertThat(ex.getErrorCode()).isEqualTo("USUARIO_NO_ENCONTRADO");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getMessage()).isNotBlank();
        }
    }

}
