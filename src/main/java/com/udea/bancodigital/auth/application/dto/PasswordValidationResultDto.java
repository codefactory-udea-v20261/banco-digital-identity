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
public class PasswordValidationResultDto {
    private boolean valid;
    private List<String> errors;
    private int strengthScore;
}
