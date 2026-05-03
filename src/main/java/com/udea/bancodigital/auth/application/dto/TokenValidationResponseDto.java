package com.udea.bancodigital.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenValidationResponseDto {
    private boolean active;
    private String sub;
    private List<String> authorities;
    private String clienteId;
    private String uid;
}
