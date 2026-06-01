package com.udea.bancodigital.auth.application.usecase;

import com.udea.bancodigital.auth.application.dto.ChangePasswordRequestDto;
import com.udea.bancodigital.auth.application.dto.ChangePasswordResponseDto;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import com.udea.bancodigital.auth.infrastructure.service.PasswordHistoryService;
import com.udea.bancodigital.auth.infrastructure.service.PasswordValidationService;
import com.udea.bancodigital.shared.exception.InvalidPasswordException;
import com.udea.bancodigital.shared.exception.PasswordChangeException;
import com.udea.bancodigital.shared.exception.UsuarioNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordValidationService passwordValidationService;
    private final PasswordHistoryService passwordHistoryService;

    /**
     * Changes the password for a user after validation
     */
    @Transactional
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

        // Escenario BDD: nueva contraseña igual a la actual
        if (passwordHistoryService.isSameAsCurrent(request.getPasswordNueva(), user)) {
            throw new PasswordChangeException("La nueva contraseña no puede ser igual a la actual");
        }

        // Historial: no reutilizar las últimas 3 contraseñas anteriores
        if (passwordHistoryService.isInHistory(request.getPasswordNueva(), user.getId())) {
            throw new PasswordChangeException(
                    "Esta contraseña ya fue utilizada anteriormente. Por favor, cree una nueva.");
        }

        // Check for password reuse
        if (passwordValidationService.isPasswordReused(request.getPasswordNueva(), usuario)) {
            throw new PasswordChangeException("No puedes usar una contraseña que ya has utilizado. Por favor, elige una contraseña diferente.");
        }

        // Update the password
        String previousHash = user.getClave();
        user.setClave(passwordValidationService.encodePassword(request.getPasswordNueva()));
        passwordHistoryService.recordPreviousPassword(user.getId(), previousHash);
        usuarioRepository.save(user);

        return ChangePasswordResponseDto.builder()
                .success(true)
                .message("Tu contraseña ha sido actualizada correctamente")
                .build();
    }
}
