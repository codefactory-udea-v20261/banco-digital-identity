package com.udea.bancodigital.shared.exception;

import org.springframework.http.HttpStatus;

public class CredencialesInvalidasException extends BusinessException {
    public CredencialesInvalidasException() {
        super("AUTH_001", "Credenciales inválidas", HttpStatus.UNAUTHORIZED);
    }
}
