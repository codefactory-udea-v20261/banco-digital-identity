package com.udea.bancodigital.shared.exception;

import org.springframework.http.HttpStatus;

public class CuentaBloqueadaException extends BusinessException {
    public CuentaBloqueadaException() {
        super("AUTH_002", "La cuenta se encuentra bloqueada por múltiples intentos fallidos", HttpStatus.FORBIDDEN);
    }
}
