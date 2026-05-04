package com.udea.bancodigital.auth.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {
    @Test
    void shouldBuildLoginRequest() {
        LoginRequest r = LoginRequest.builder().correo("a@b.c").clave("pw").mfaCode("123").build();
        assertThat(r.getCorreo()).isEqualTo("a@b.c");
        assertThat(r.getClave()).isEqualTo("pw");
        assertThat(r.getMfaCode()).isEqualTo("123");
        assertThat(r.toString()).contains("a@b.c");
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        LoginRequest r1 = LoginRequest.builder().correo("a@b.c").clave("pw").build();
        LoginRequest r2 = LoginRequest.builder().correo("a@b.c").clave("pw").build();
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }
}
