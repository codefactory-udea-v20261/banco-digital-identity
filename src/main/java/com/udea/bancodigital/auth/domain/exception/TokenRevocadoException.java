package com.udea.bancodigital.auth.domain.exception;

import com.udea.bancodigital.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * El token enviado existe en la tabla token_revocado (blacklist).
 */
public class TokenRevocadoException extends BusinessException {

    public TokenRevocadoException() {
        super("TOKEN_REVOCADO",
              "El token de acceso ha sido revocado. Por favor inicie sesión nuevamente",
              HttpStatus.UNAUTHORIZED);
    }
}
