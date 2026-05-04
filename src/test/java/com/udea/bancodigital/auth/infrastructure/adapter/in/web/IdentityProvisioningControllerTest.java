package com.udea.bancodigital.auth.infrastructure.adapter.in.web;

import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessRequestDto;
import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessResponseDto;
import com.udea.bancodigital.auth.domain.port.in.ProvisionClientAccessPort;
import com.udea.bancodigital.shared.web.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityProvisioningControllerTest {

    @Mock
    private ProvisionClientAccessPort provisionClientAccessPort;

    @InjectMocks
    private IdentityProvisioningController controller;

    @Test
    @DisplayName("Debe provisionar acceso de cliente")
    void provisionClientAccess_ShouldReturn200() {
        ProvisionClientAccessRequestDto req = ProvisionClientAccessRequestDto.builder()
                .clienteId(UUID.randomUUID()).email("c@t.com").build();
        ProvisionClientAccessResponseDto resp = ProvisionClientAccessResponseDto.builder()
                .userId(UUID.randomUUID()).status("ACTIVE").build();
        when(provisionClientAccessPort.provisionClientAccess(any())).thenReturn(resp);

        ResponseEntity<ApiResponse<ProvisionClientAccessResponseDto>> response = controller.provisionClientAccess(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Debe provisionar simple y retornar 200 vacío")
    void provisionSimple_ShouldReturn200() {
        ProvisionClientAccessRequestDto req = ProvisionClientAccessRequestDto.builder()
                .clienteId(UUID.randomUUID()).email("c@t.com").build();
        ProvisionClientAccessResponseDto resp = ProvisionClientAccessResponseDto.builder().build();
        when(provisionClientAccessPort.provisionClientAccess(any())).thenReturn(resp);

        ResponseEntity<Void> response = controller.provisionSimple(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Debe verificar existencia de email")
    void existsByEmail_ShouldReturnFalse() {
        ResponseEntity<Map<String, Object>> response = controller.existsByEmail("test@test.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("exists")).isEqualTo(false);
    }
}
