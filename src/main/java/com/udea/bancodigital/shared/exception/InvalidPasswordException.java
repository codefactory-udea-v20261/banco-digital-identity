package com.udea.bancodigital.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends BusinessException {
    public InvalidPasswordException(String message) {
        super("INVALID_PASSWORD", message, HttpStatus.BAD_REQUEST);
    }
}
