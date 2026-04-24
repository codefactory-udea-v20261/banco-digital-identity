package com.udea.bancodigital.shared.exception;

import org.springframework.http.HttpStatus;

public class MfaRequeridoException extends BusinessException {
    public MfaRequeridoException() {
        super("AUTH_003", "Se requiere código de autenticación multifactor (MFA) para este rol", HttpStatus.UNAUTHORIZED);
    }
}
