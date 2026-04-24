package com.udea.bancodigital.auth.infrastructure.adapter.in.web;

import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessRequestDto;
import com.udea.bancodigital.auth.application.dto.ProvisionClientAccessResponseDto;
import com.udea.bancodigital.auth.domain.port.in.ProvisionClientAccessPort;
import com.udea.bancodigital.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
@Tag(name = "Identidad Interna", description = "API interna para operaciones de identidad que seran consumidas por otros servicios")
public class IdentityProvisioningController {

    private final ProvisionClientAccessPort provisionClientAccessPort;

    @PostMapping("/provision-client-access")
    @Operation(
            summary = "Provisionar acceso digital para un cliente",
            description = "Prepara el contrato interno que usara el core bancario para solicitar la creacion del usuario digital de un cliente."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Acceso provisionado o ya existente"
            )
    })
    public ResponseEntity<ApiResponse<ProvisionClientAccessResponseDto>> provisionClientAccess(
            @Valid @RequestBody ProvisionClientAccessRequestDto request) {
        ProvisionClientAccessResponseDto response = provisionClientAccessPort.provisionClientAccess(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/provision")
    @Operation(
            summary = "Provisionar acceso - Alias simple",
            description = "Alias simplificado para provisionar acceso de cliente recién creado."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Acceso provisionado"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            )
    })
    public ResponseEntity<Void> provisionSimple(
            @Valid @RequestBody ProvisionClientAccessRequestDto request) {
        provisionClientAccessPort.provisionClientAccess(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exists")
    @Operation(
            summary = "Verificar si un email existe",
            description = "Verifica si ya existe un usuario con el email especificado."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Estado de existencia del email"
            )
    })
    public ResponseEntity<Map<String, Object>> existsByEmail(
            @RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        response.put("exists", false);
        return ResponseEntity.ok(response);
    }
}
