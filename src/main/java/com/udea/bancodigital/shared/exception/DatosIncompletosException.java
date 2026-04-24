package com.udea.bancodigital.shared.exception;

import com.udea.bancodigital.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DatosIncompletosException extends BusinessException {

    public DatosIncompletosException(String message) {
        super(
                "DATOS_INCOMPLETOS",
                message,
                HttpStatus.BAD_REQUEST
        );
    }
}
