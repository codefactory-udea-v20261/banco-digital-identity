package com.udea.bancodigital.auth.domain.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;

class TokenRevocadoExceptionTest {
    @Test
    void shouldCreateException() {
        TokenRevocadoException ex = new TokenRevocadoException();
        assertThat(ex.getErrorCode()).isEqualTo("TOKEN_REVOCADO");
        assertThat(ex.getMessage()).contains("revocado");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
