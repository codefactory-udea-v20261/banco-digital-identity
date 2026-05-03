package com.udea.bancodigital.auth.infrastructure.service;

import com.udea.bancodigital.auth.application.dto.PasswordValidationResultDto;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordValidationService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioJpaRepository usuarioRepository;

    @Value("${app.security.password-policy.min-length:8}")
    private int minLength;

    @Value("${app.security.password-policy.require-uppercase:true}")
    private boolean requireUppercase;

    @Value("${app.security.password-policy.require-lowercase:true}")
    private boolean requireLowercase;

    @Value("${app.security.password-policy.require-numbers:true}")
    private boolean requireNumbers;

    @Value("${app.security.password-policy.require-special-chars:true}")
    private boolean requireSpecialChars;

    @Value("${app.security.password-policy.prevent-reuse:true}")
    private boolean preventReuse;

    /**
     * Validates password strength against configured policy
     */
    public PasswordValidationResultDto validate(String password) {
        List<String> errors = new ArrayList<>();
        int strengthScore = 0;

        // Check length
        if (password == null || password.length() < minLength) {
            errors.add("La contraseña debe tener al menos " + minLength + " caracteres");
        } else {
            strengthScore += 20;
        }

        // Check uppercase
        if (requireUppercase && !Pattern.compile("[A-Z]").matcher(password).find()) {
            errors.add("La contraseña debe contener al menos una letra mayúscula");
        } else if (Pattern.compile("[A-Z]").matcher(password).find()) {
            strengthScore += 20;
        }

        // Check lowercase
        if (requireLowercase && !Pattern.compile("[a-z]").matcher(password).find()) {
            errors.add("La contraseña debe contener al menos una letra minúscula");
        } else if (Pattern.compile("[a-z]").matcher(password).find()) {
            strengthScore += 20;
        }

        // Check numbers
        if (requireNumbers && !Pattern.compile("[0-9]").matcher(password).find()) {
            errors.add("La contraseña debe contener al menos un número");
        } else if (Pattern.compile("[0-9]").matcher(password).find()) {
            strengthScore += 20;
        }

        // Check special characters
        if (requireSpecialChars && !Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find()) {
            errors.add("La contraseña debe contener al menos un carácter especial (!@#$%^&* etc)");
        } else if (Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find()) {
            strengthScore += 20;
        }

        return PasswordValidationResultDto.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .strengthScore(strengthScore)
                .build();
    }

    /**
     * Validates old password against current user password
     */
    public boolean validateOldPassword(String oldPassword, String encodedPassword) {
        return passwordEncoder.matches(oldPassword, encodedPassword);
    }

    /**
     * Checks if new password matches confirmation
     */
    public boolean passwordsMatch(String password, String confirmation) {
        return password != null && password.equals(confirmation);
    }

    /**
     * Checks if password has been used before (basic check - would need password history table for full implementation)
     */
    public boolean isPasswordReused(String password, Optional<UsuarioEntity> usuario) {
        if (usuario.isEmpty()) {
            return false;
        }
        // In a full implementation, this would check against password history table
        // For now, we prevent reusing the current password
        return passwordEncoder.matches(password, usuario.get().getClave());
    }

    /**
     * Encodes password using configured password encoder
     */
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Gets password strength description
     */
    public String getStrengthDescription(int score) {
        if (score >= 90) return "Muy Fuerte";
        if (score >= 70) return "Fuerte";
        if (score >= 50) return "Moderada";
        if (score >= 30) return "Débil";
        return "Muy Débil";
    }
}
