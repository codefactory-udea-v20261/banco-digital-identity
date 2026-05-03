package com.udea.bancodigital.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequestDto {
    @NotBlank(message = "El token de refresco es obligatorio")
    private String refreshToken;
}