package com.udea.bancodigital.shared.exception;

import org.springframework.http.HttpStatus;

public class ClienteYaExisteException extends BusinessException {

    public ClienteYaExisteException(String campo, String valor) {
        super(
                "CLIENTE_YA_EXISTE",
                "Ya existe un cliente con " + campo + ": " + valor,
                HttpStatus.CONFLICT
        );
    }
}
