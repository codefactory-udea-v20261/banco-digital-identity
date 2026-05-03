package com.udea.bancodigital.shared.exception;

import org.springframework.http.HttpStatus;

public class PasswordChangeException extends BusinessException {
    public PasswordChangeException(String message) {
        super("PASSWORD_CHANGE_ERROR", message, HttpStatus.BAD_REQUEST);
    }
}
