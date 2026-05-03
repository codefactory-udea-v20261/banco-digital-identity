package com.udea.bancodigital.shared.exception;

import org.springframework.http.HttpStatus;

public class UsuarioNoEncontradoException extends BusinessException {
    public UsuarioNoEncontradoException() {
        super("USUARIO_NO_ENCONTRADO", "El usuario no fue encontrado", HttpStatus.NOT_FOUND);
    }
}
