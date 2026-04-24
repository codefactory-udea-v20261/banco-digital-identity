package com.udea.bancodigital.auth.application.usecase;

import com.udea.bancodigital.auth.application.dto.LoginRequestDto;
import com.udea.bancodigital.auth.application.dto.LoginResponseDto;
import com.udea.bancodigital.auth.application.dto.LogoutRequestDto;
import com.udea.bancodigital.auth.domain.model.Rol;
import com.udea.bancodigital.auth.domain.model.Usuario;
import com.udea.bancodigital.auth.domain.port.in.AuthPort;
import com.udea.bancodigital.auth.domain.port.out.JwtProviderPort;
import com.udea.bancodigital.auth.domain.port.out.PasswordEncoderPort;
import com.udea.bancodigital.auth.domain.port.out.TokenBlacklistPort;
import com.udea.bancodigital.auth.domain.port.out.UsuarioRepositoryPort;
import com.udea.bancodigital.shared.exception.CredencialesInvalidasException;
import com.udea.bancodigital.shared.exception.CuentaBloqueadaException;
import com.udea.bancodigital.shared.exception.MfaRequeridoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class AuthUseCase implements AuthPort {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final JwtProviderPort jwtProviderPort;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Value("${app.security.login.max-failed-attempts:3}")
    private int maxFailedAttempts = 3;

    public AuthUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                        PasswordEncoderPort passwordEncoderPort,
                        JwtProviderPort jwtProviderPort,
                        TokenBlacklistPort tokenBlacklistPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.jwtProviderPort = jwtProviderPort;
        this.tokenBlacklistPort = tokenBlacklistPort;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        Usuario usuario = usuarioRepositoryPort.findByEmail(request.getCorreo())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isBloqueado() && usuario.getIntentosFallidos() != null
                && usuario.getIntentosFallidos() >= maxFailedAttempts) {
            usuario = resetFailedAttempts(usuario);
            usuarioRepositoryPort.save(usuario);
        }

        if (!usuario.isActivo() || usuario.isBloqueado()) {
            throw new CuentaBloqueadaException();
        }

        if (!passwordEncoderPort.matches(request.getClave(), usuario.getClave())) {
            int intentos = usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos();
            intentos++;
            
            Usuario usuarioActualizado = Usuario.builder()
                    .id(usuario.getId())
                    .clienteId(usuario.getClienteId())
                    .correo(usuario.getCorreo())
                    .clave(usuario.getClave())
                    .activo(usuario.isActivo())
                    .bloqueado(intentos >= maxFailedAttempts)
                    .intentosFallidos(intentos)
                    .secretoMfa(usuario.getSecretoMfa())
                    .mfaActivo(usuario.isMfaActivo())
                    .roles(usuario.getRoles())
                    .build();
            
            usuarioRepositoryPort.save(usuarioActualizado);
            
            if (usuarioActualizado.isBloqueado()) {
                throw new CuentaBloqueadaException();
            }
            throw new CredencialesInvalidasException();
        }

        boolean requiresMfa = requiresMfa(usuario);
        if (requiresMfa) {
            if (request.getMfaCode() == null || request.getMfaCode().isBlank() || !request.getMfaCode().equals(usuario.getSecretoMfa())) {
                throw new MfaRequeridoException();
            }
        }

        if (usuario.getIntentosFallidos() != null && usuario.getIntentosFallidos() > 0) {
            Usuario usuarioActualizado = resetFailedAttempts(usuario);
            usuarioRepositoryPort.save(usuarioActualizado);
        }

        String token = jwtProviderPort.generateToken(usuario);
        return new LoginResponseDto(token);
    }

    @Override
    public void logout(LogoutRequestDto request) {
        String token = request.getToken();
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String jti = jwtProviderPort.extractJti(token);
        Instant expiration = jwtProviderPort.extractExpiration(token).toInstant();
        tokenBlacklistPort.revoke(jti, jwtProviderPort.extractUserId(token), expiration);
    }

    private boolean requiresMfa(Usuario usuario) {
        Set<Rol> roles = usuario.getRoles();
        if (!usuario.isMfaActivo() || roles == null) {
            return false;
        }
        return roles.stream().anyMatch(rol -> 
            "ADMIN".equalsIgnoreCase(rol.getNombre()) || "AUDITOR".equalsIgnoreCase(rol.getNombre())
        );
    }

    private Usuario resetFailedAttempts(Usuario usuario) {
        return Usuario.builder()
                .id(usuario.getId())
                .clienteId(usuario.getClienteId())
                .correo(usuario.getCorreo())
                .clave(usuario.getClave())
                .activo(usuario.isActivo())
                .bloqueado(false)
                .intentosFallidos(0)
                .secretoMfa(usuario.getSecretoMfa())
                .mfaActivo(usuario.isMfaActivo())
                .roles(usuario.getRoles())
                .build();
    }
}
