package com.udea.bancodigital.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticatedUserResponseDto {
    private UUID userId;
    private String username;
    private UUID clienteId;
    private List<String> roles;
}
