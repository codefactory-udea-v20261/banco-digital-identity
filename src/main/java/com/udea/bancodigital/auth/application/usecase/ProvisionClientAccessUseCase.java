package com.udea.bancodigital.auth.application.usecase;

import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessRequestDto;
import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessResponseDto;
import com.udea.bancodigital.auth.domain.model.Rol;
import com.udea.bancodigital.auth.domain.model.Usuario;
import com.udea.bancodigital.auth.domain.port.in.ProvisionClientAccessPort;
import com.udea.bancodigital.auth.domain.port.out.PasswordEncoderPort;
import com.udea.bancodigital.auth.domain.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProvisionClientAccessUseCase implements ProvisionClientAccessPort {

    private static final short CLIENT_ROLE_ID = 3;
    private static final String CLIENT_ROLE_NAME = "CLIENTE";
    private static final String STATUS_PROVISIONED = "PROVISIONED";
    private static final String STATUS_ALREADY_EXISTS = "ALREADY_EXISTS";

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Value("${app.security.default-client-password:Temp1234!}")
    private String defaultClientPassword;

    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepositoryPort.existsByUsername(email);
    }

    @Override
    public ProvisionClientAccessResponseDto provisionClientAccess(ProvisionClientAccessRequestDto request) {
        if (existsByEmail(request.getEmail())) {
            Usuario usuarioExistente = usuarioRepositoryPort.findByEmail(request.getEmail()).orElse(null);
            return ProvisionClientAccessResponseDto.builder()
                    .userId(usuarioExistente != null ? usuarioExistente.getId() : null)
                    .clienteId(request.getClienteId())
                    .email(request.getEmail())
                    .status(STATUS_ALREADY_EXISTS)
                    .build();
        }

        Usuario usuarioCliente = Usuario.builder()
                .id(UUID.randomUUID())
                .clienteId(request.getClienteId())
                .correo(request.getEmail())
                .clave(passwordEncoderPort.encode(defaultClientPassword))
                .activo(true)
                .bloqueado(false)
                .intentosFallidos(0)
                .mfaActivo(false)
                .roles(Set.of(Rol.builder()
                        .id(CLIENT_ROLE_ID)
                        .nombre(CLIENT_ROLE_NAME)
                        .build()))
                .build();

        Usuario usuarioGuardado = usuarioRepositoryPort.save(usuarioCliente);

        return ProvisionClientAccessResponseDto.builder()
                .userId(usuarioGuardado.getId())
                .clienteId(usuarioGuardado.getClienteId())
                .email(usuarioGuardado.getCorreo())
                .status(STATUS_PROVISIONED)
                .build();
    }
}
