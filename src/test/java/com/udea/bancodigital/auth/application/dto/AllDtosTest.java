package com.udea.bancodigital.auth.application.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class AllDtosTest {

    @Test
    void testChangePasswordRequestDto() {
        var dto = ChangePasswordRequestDto.builder().passwordActual("o").passwordNueva("n").passwordConfirmacion("n").build();
        var dto2 = ChangePasswordRequestDto.builder().passwordActual("o").passwordNueva("n").passwordConfirmacion("n").build();
        assertThat(dto.getPasswordActual()).isEqualTo("o");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("o");
        dto.setPasswordActual("x");
        assertThat(dto.getPasswordActual()).isEqualTo("x");
        assertThat(new ChangePasswordRequestDto()).isNotNull();
        assertThat(dto).isNotEqualTo(dto2);
    }

    @Test
    void testChangePasswordResponseDto() {
        var dto = ChangePasswordResponseDto.builder().success(true).message("OK").build();
        var dto2 = ChangePasswordResponseDto.builder().success(true).message("OK").build();
        assertThat(dto.isSuccess()).isTrue();
        assertThat(dto.getMessage()).isEqualTo("OK");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("OK");
        dto.setSuccess(false);
        assertThat(dto.isSuccess()).isFalse();
        assertThat(new ChangePasswordResponseDto()).isNotNull();
    }

    @Test
    void testLoginRequestDto() {
        var dto = LoginRequestDto.builder().correo("a@b.c").clave("pw").mfaCode("123").build();
        var dto2 = LoginRequestDto.builder().correo("a@b.c").clave("pw").mfaCode("123").build();
        assertThat(dto.getCorreo()).isEqualTo("a@b.c");
        assertThat(dto.getClave()).isEqualTo("pw");
        assertThat(dto.getMfaCode()).isEqualTo("123");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("a@b.c");
        dto.setMfaCode(null);
        assertThat(dto.getMfaCode()).isNull();
        assertThat(new LoginRequestDto()).isNotNull();
    }

    @Test
    void testLoginResponseDto() {
        var dto = LoginResponseDto.builder().token("t").refreshToken("r").build();
        var dto2 = LoginResponseDto.builder().token("t").refreshToken("r").build();
        assertThat(dto.getToken()).isEqualTo("t");
        assertThat(dto.getRefreshToken()).isEqualTo("r");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("t");
        dto.setToken("new");
        assertThat(dto.getToken()).isEqualTo("new");
        assertThat(new LoginResponseDto()).isNotNull();
    }

    @Test
    void testLogoutRequestDto() {
        var dto = LogoutRequestDto.builder().token("tok").build();
        var dto2 = LogoutRequestDto.builder().token("tok").build();
        assertThat(dto.getToken()).isEqualTo("tok");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("tok");
        dto.setToken("new");
        assertThat(dto.getToken()).isEqualTo("new");
        assertThat(new LogoutRequestDto()).isNotNull();
    }

    @Test
    void testPasswordValidationResultDto() {
        var dto = PasswordValidationResultDto.builder().valid(true).errors(List.of()).strengthScore(80).build();
        var dto2 = PasswordValidationResultDto.builder().valid(true).errors(List.of()).strengthScore(80).build();
        assertThat(dto.isValid()).isTrue();
        assertThat(dto.getErrors()).isEmpty();
        assertThat(dto.getStrengthScore()).isEqualTo(80);
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("80");
        dto.setValid(false);
        assertThat(dto.isValid()).isFalse();
        assertThat(new PasswordValidationResultDto()).isNotNull();
    }

    @Test
    void testProvisionClientAccessRequestDto() {
        UUID cid = UUID.randomUUID();
        var dto = ProvisionClientAccessRequestDto.builder().clienteId(cid).email("a@b.c").build();
        var dto2 = ProvisionClientAccessRequestDto.builder().clienteId(cid).email("a@b.c").build();
        assertThat(dto.getClienteId()).isEqualTo(cid);
        assertThat(dto.getEmail()).isEqualTo("a@b.c");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("a@b.c");
        dto.setEmail("x@y.z");
        assertThat(dto.getEmail()).isEqualTo("x@y.z");
        assertThat(new ProvisionClientAccessRequestDto()).isNotNull();
    }

    @Test
    void testRefreshRequestDto() {
        var dto = RefreshRequestDto.builder().refreshToken("rt").build();
        var dto2 = RefreshRequestDto.builder().refreshToken("rt").build();
        assertThat(dto.getRefreshToken()).isEqualTo("rt");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("rt");
        dto.setRefreshToken("new");
        assertThat(dto.getRefreshToken()).isEqualTo("new");
        assertThat(new RefreshRequestDto()).isNotNull();
    }

    @Test
    void testTokenValidationResponseDto() {
        var dto = TokenValidationResponseDto.builder().active(true).sub("u")
                .authorities(List.of("ROLE_ADMIN")).clienteId("c").uid("id").build();
        var dto2 = TokenValidationResponseDto.builder().active(true).sub("u")
                .authorities(List.of("ROLE_ADMIN")).clienteId("c").uid("id").build();
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getSub()).isEqualTo("u");
        assertThat(dto.getAuthorities()).contains("ROLE_ADMIN");
        assertThat(dto.getClienteId()).isEqualTo("c");
        assertThat(dto.getUid()).isEqualTo("id");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("ROLE_ADMIN");
        dto.setActive(false);
        assertThat(dto.isActive()).isFalse();
        assertThat(new TokenValidationResponseDto()).isNotNull();
    }

    @Test
    void testProvisionClientAccessResponseDto() {
        UUID uid = UUID.randomUUID();
        UUID cid = UUID.randomUUID();
        var dto = ProvisionClientAccessResponseDto.builder().userId(uid).clienteId(cid)
                .email("e@t.com").status("ACTIVE").build();
        var dto2 = ProvisionClientAccessResponseDto.builder().userId(uid).clienteId(cid)
                .email("e@t.com").status("ACTIVE").build();
        assertThat(dto.getUserId()).isEqualTo(uid);
        assertThat(dto.getClienteId()).isEqualTo(cid);
        assertThat(dto.getEmail()).isEqualTo("e@t.com");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("ACTIVE");
    }

    @Test
    void testAuthenticatedUserResponseDto() {
        var dto = AuthenticatedUserResponseDto.builder().username("admin")
                .roles(List.of("ADMIN")).build();
        var dto2 = AuthenticatedUserResponseDto.builder().username("admin")
                .roles(List.of("ADMIN")).build();
        assertThat(dto.getUsername()).isEqualTo("admin");
        assertThat(dto.getRoles()).contains("ADMIN");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains("admin");
    }
}
