package com.udea.bancodigital.auth.application.usecase;

import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessRequestDto;
import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessResponseDto;
import com.udea.bancodigital.auth.domain.model.Usuario;
import com.udea.bancodigital.auth.domain.port.out.PasswordEncoderPort;
import com.udea.bancodigital.auth.domain.port.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProvisionClientAccessUseCaseTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private ProvisionClientAccessUseCase useCase;

    @Test
    void deberiaProvisionarAccesoCuandoElUsuarioNoExiste() {
        UUID clienteId = UUID.randomUUID();
        ProvisionClientAccessRequestDto request = ProvisionClientAccessRequestDto.builder()
                .clienteId(clienteId)
                .email("cliente@test.com")
                .build();

        ReflectionTestUtils.setField(useCase, "defaultClientPassword", "Temp1234!");
        when(usuarioRepositoryPort.existsByUsername(request.getEmail())).thenReturn(false);
        when(passwordEncoderPort.encode("Temp1234!")).thenReturn("encoded-password");
        when(usuarioRepositoryPort.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProvisionClientAccessResponseDto response = useCase.provisionClientAccess(request);

        assertThat(response.getClienteId()).isEqualTo(clienteId);
        assertThat(response.getEmail()).isEqualTo("cliente@test.com");
        assertThat(response.getStatus()).isEqualTo("PROVISIONED");
        verify(usuarioRepositoryPort).save(any(Usuario.class));
    }

    @Test
    void deberiaRetornarAlreadyExistsCuandoElUsuarioYaExiste() {
        UUID clienteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ProvisionClientAccessRequestDto request = ProvisionClientAccessRequestDto.builder()
                .clienteId(clienteId)
                .email("cliente@test.com")
                .build();

        when(usuarioRepositoryPort.existsByUsername(request.getEmail())).thenReturn(true);
        when(usuarioRepositoryPort.findByEmail(request.getEmail())).thenReturn(Optional.of(
                Usuario.builder()
                        .id(userId)
                        .clienteId(clienteId)
                        .correo(request.getEmail())
                        .roles(Set.of())
                        .build()
        ));

        ProvisionClientAccessResponseDto response = useCase.provisionClientAccess(request);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getStatus()).isEqualTo("ALREADY_EXISTS");
    }
}
