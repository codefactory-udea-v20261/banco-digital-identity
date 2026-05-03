package com.udea.bancodigital.auth.infrastructure.adapter.in.web;

import com.udea.bancodigital.auth.application.dto.LoginRequestDto;
import com.udea.bancodigital.auth.application.dto.LoginResponseDto;
import com.udea.bancodigital.auth.application.dto.LogoutRequestDto;
import com.udea.bancodigital.auth.application.dto.RefreshRequestDto;
import com.udea.bancodigital.auth.application.dto.TokenValidationResponseDto;
import com.udea.bancodigital.auth.application.dto.ChangePasswordRequestDto;
import com.udea.bancodigital.auth.application.dto.ChangePasswordResponseDto;
import com.udea.bancodigital.auth.application.usecase.ChangePasswordUseCase;
import com.udea.bancodigital.auth.domain.port.in.AuthPort;
import com.udea.bancodigital.auth.infrastructure.config.JwtProvider;
import com.udea.bancodigital.shared.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "API para autenticacion, emision y revocacion de tokens JWT")
public class AuthController {

    private final AuthPort authPort;
    private final JwtProvider jwtProvider;
    private final ChangePasswordUseCase changePasswordUseCase;

    public AuthController(AuthPort authPort, JwtProvider jwtProvider, ChangePasswordUseCase changePasswordUseCase) {
        this.authPort = authPort;
        this.jwtProvider = jwtProvider;
        this.changePasswordUseCase = changePasswordUseCase;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuario",
            description = "Valida credenciales y retorna un token JWT para consumir las APIs protegidas."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Autenticacion exitosa",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Credenciales con formato invalido",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Credenciales invalidas",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authPort.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refrescar token",
            description = "Verifica el refresh token y retorna un nuevo par de tokens."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refrescado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token invalido o expirado",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<LoginResponseDto> refresh(@Valid @RequestBody RefreshRequestDto request) {
        LoginResponseDto response = authPort.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-token")
    @Operation(
            summary = "Validar token JWT",
            description = "Endpoint interno utilizado por otros microservicios para validar la validez de un token y extraer sus claims."
    )
    public ResponseEntity<TokenValidationResponseDto> validateToken(@RequestBody String token) {
        try {
            if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }

            if (!jwtProvider.isTokenValid(token)) {
                return ResponseEntity.ok(TokenValidationResponseDto.builder().active(false).build());
            }

            Claims claims = jwtProvider.getClaims(token);
            List<String> roles = (List<String>) claims.get("roles");
            List<String> permissions = (List<String>) claims.get("permissions");
            
            // Map to standard ROLE_ prefix expected by Spring Security in other services
            List<String> authorities = new ArrayList<>();
            if (roles != null) {
                authorities.addAll(roles.stream()
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .toList());
            }
            if (permissions != null) {
                authorities.addAll(permissions);
            }

            return ResponseEntity.ok(TokenValidationResponseDto.builder()
                    .active(true)
                    .sub(claims.getSubject())
                    .authorities(authorities)
                    .clienteId((String) claims.get("clienteId"))
                    .uid((String) claims.get("uid"))
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(TokenValidationResponseDto.builder().active(false).build());
        }
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesion",
            description = "Revoca el token JWT enviado en el encabezado Authorization para impedir su reutilizacion."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Sesion cerrada exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Encabezado Authorization ausente o invalido",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token invalido o expirado",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        LogoutRequestDto request = new LogoutRequestDto();
        request.setToken(authorizationHeader);
        authPort.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Cambiar contraseña",
            description = "Permite a un usuario autenticado cambiar su contraseña actual por una nueva, con validación de requisitos de seguridad."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Contraseña cambiada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChangePasswordResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validación fallida (contraseña débil, no coinciden, etc.)",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Contraseña actual incorrecta o token inválido",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ChangePasswordResponseDto> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request) {
        // Authenticate user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            return ResponseEntity.status(401).build();
        }
        
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        
        // Call use case
        ChangePasswordResponseDto response = changePasswordUseCase.changePassword(principal.userId(), request);
        return ResponseEntity.ok(response);
    }
}
