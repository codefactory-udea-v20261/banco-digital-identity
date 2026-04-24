package com.udea.bancodigital.auth.infrastructure.adapter.in.web;

import com.udea.bancodigital.auth.application.dto.AuthenticatedUserResponseDto;
import com.udea.bancodigital.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Identidad", description = "API para consultar la identidad autenticada y exponer capacidades del futuro identity-service")
public class IdentityController {

    @GetMapping("/me")
    @Operation(
            summary = "Consultar identidad autenticada",
            description = "Retorna la identidad resuelta a partir del contexto de seguridad sin exponer detalles internos del token."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Identidad autenticada obtenida exitosamente"
            )
    })
    public ResponseEntity<AuthenticatedUserResponseDto> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();

        List<String> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();

        return ResponseEntity.ok(AuthenticatedUserResponseDto.builder()
                .userId(principal.userId())
                .username(principal.username())
                .clienteId(principal.clienteId())
                .roles(roles)
                .build());
    }
}
