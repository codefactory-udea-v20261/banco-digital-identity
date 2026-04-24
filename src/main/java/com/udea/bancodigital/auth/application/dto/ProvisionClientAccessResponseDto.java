package com.udea.bancodigital.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProvisionClientAccessResponseDto {
    private UUID userId;
    private UUID clienteId;
    private String email;
    private String status;
}
