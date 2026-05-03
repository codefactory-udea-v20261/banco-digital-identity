package com.udea.bancodigital.auth.application.usecase;

import com.udea.bancodigital.auth.application.dto.ChangePasswordRequestDto;
import com.udea.bancodigital.auth.application.dto.ChangePasswordResponseDto;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import com.udea.bancodigital.auth.infrastructure.service.PasswordValidationService;
import com.udea.bancodigital.shared.exception.InvalidPasswordException;
import com.udea.bancodigital.shared.exception.PasswordChangeException;
import com.udea.bancodigital.shared.exception.UsuarioNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordValidationService passwordValidationService;

    /**
     * Changes the password for a user after validation
     */
    public ChangePasswordResponseDto changePassword(UUID usuarioId, ChangePasswordRequestDto request) {
        // Find the usuario
        Optional<UsuarioEntity> usuario = usuarioRepository.findById(usuarioId);
        if (usuario.isEmpty()) {
            throw new UsuarioNoEncontradoException();
        }

        UsuarioEntity user = usuario.get();

        // Validate old password
        if (!passwordValidationService.validateOldPassword(request.getPasswordActual(), user.getClave())) {
            throw new InvalidPasswordException("La contraseña actual es incorrecta");
        }

        // Validate passwords match
        if (!passwordValidationService.passwordsMatch(request.getPasswordNueva(), request.getPasswordConfirmacion())) {
            throw new PasswordChangeException("Las nuevas contraseñas no coinciden");
        }

        // Validate password strength
        var validationResult = passwordValidationService.validate(request.getPasswordNueva());
        if (!validationResult.isValid()) {
            throw new PasswordChangeException("La contraseña no cumple los requisitos de seguridad: " + String.join(", ", validationResult.getErrors()));
        }

        // Check for password reuse
        if (passwordValidationService.isPasswordReused(request.getPasswordNueva(), usuario)) {
            throw new PasswordChangeException("No puedes usar una contraseña que ya has utilizado. Por favor, elige una contraseña diferente.");
        }

        // Update the password
        user.setClave(passwordValidationService.encodePassword(request.getPasswordNueva()));
        usuarioRepository.save(user);

        return ChangePasswordResponseDto.builder()
                .success(true)
                .message("Tu contraseña ha sido actualizada correctamente")
                .build();
    }
}
