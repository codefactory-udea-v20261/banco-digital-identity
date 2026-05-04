package com.udea.bancodigital.shared.exception;

import com.udea.bancodigital.shared.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("Debe manejar BusinessException")
    void handleBusinessException() {
        when(request.getRequestURI()).thenReturn("/api/test");
        CredencialesInvalidasException ex = new CredencialesInvalidasException();
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleCuentaBloqueada() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<ApiResponse<Void>> r = handler.handleBusinessException(new CuentaBloqueadaException(), request);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleMfaRequerido() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<ApiResponse<Void>> r = handler.handleBusinessException(new MfaRequeridoException(), request);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleUsuarioNoEncontrado() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<ApiResponse<Void>> r = handler.handleBusinessException(new UsuarioNoEncontradoException(), request);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleDatosIncompletos() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<ApiResponse<Void>> r = handler.handleBusinessException(new DatosIncompletosException("missing"), request);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleInvalidPassword() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<ApiResponse<Void>> r = handler.handleBusinessException(new InvalidPasswordException("weak"), request);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handlePasswordChange() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<ApiResponse<Void>> r = handler.handleBusinessException(new PasswordChangeException("mismatch"), request);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleClienteYaExiste() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<ApiResponse<Void>> r = handler.handleBusinessException(new ClienteYaExisteException("correo", "a@b.c"), request);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleValidationException() {
        when(request.getRequestURI()).thenReturn("/api/test");
        BindingResult br = mock(BindingResult.class);
        when(br.getFieldErrors()).thenReturn(List.of(new FieldError("o", "f", "required")));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, br);
        ResponseEntity<ApiResponse<Void>> r = handler.handleValidationException(ex, request);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleGenericException() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<ApiResponse<Void>> r = handler.handleGenericException(new RuntimeException("boom"), request);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
