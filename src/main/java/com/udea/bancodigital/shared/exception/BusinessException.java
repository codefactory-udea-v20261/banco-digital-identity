package com.udea.bancodigital.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción base para todas las excepciones de negocio del dominio.
 * Las excepciones específicas heredan de esta clase.
 * <p>
 * Ejemplo:
 *   public class ClienteNoEncontradoException extends BusinessException { ... }
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    protected BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
