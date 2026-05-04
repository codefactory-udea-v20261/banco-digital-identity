package com.udea.bancodigital.auth.application.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    @Test
    void testProvisionClientAccessResponseDto() {
        UUID userId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        ProvisionClientAccessResponseDto dto = ProvisionClientAccessResponseDto.builder()
                .userId(userId)
                .clienteId(clienteId)
                .email("test@test.com")
                .status("ACTIVE")
                .build();

        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getClienteId()).isEqualTo(clienteId);
        assertThat(dto.getEmail()).isEqualTo("test@test.com");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");

        dto.setStatus("INACTIVE");
        assertThat(dto.getStatus()).isEqualTo("INACTIVE");

        assertThat(dto.toString()).isNotBlank();
        assertThat(dto.hashCode()).isNotZero();
        assertThat(dto).isEqualTo(dto);
        assertThat(dto).isNotEqualTo(new Object());
    }

    @Test
    void testAuthenticatedUserResponseDto() {
        UUID userId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        AuthenticatedUserResponseDto dto = AuthenticatedUserResponseDto.builder()
                .userId(userId)
                .username("test@test.com")
                .clienteId(clienteId)
                .roles(List.of("ADMIN"))
                .build();

        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getUsername()).isEqualTo("test@test.com");
        assertThat(dto.getClienteId()).isEqualTo(clienteId);
        assertThat(dto.getRoles()).contains("ADMIN");

        dto.setUsername("new@test.com");
        assertThat(dto.getUsername()).isEqualTo("new@test.com");

        assertThat(dto.toString()).isNotBlank();
        assertThat(dto.hashCode()).isNotZero();
        assertThat(dto).isEqualTo(dto);
        assertThat(dto).isNotEqualTo(new Object());
    }
}
