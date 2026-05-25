package com.udea.bancodigital.auth.application.usecase;

import com.udea.bancodigital.auth.application.dto.ChangePasswordRequestDto;
import com.udea.bancodigital.auth.application.dto.ChangePasswordResponseDto;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import com.udea.bancodigital.auth.infrastructure.service.PasswordHistoryService;
import com.udea.bancodigital.auth.infrastructure.service.PasswordValidationService;
import com.udea.bancodigital.auth.application.dto.PasswordValidationResultDto;
import com.udea.bancodigital.shared.exception.InvalidPasswordException;
import com.udea.bancodigital.shared.exception.PasswordChangeException;
import com.udea.bancodigital.shared.exception.UsuarioNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

    @Mock
    private UsuarioJpaRepository usuarioRepository;

    @Mock
    private PasswordValidationService passwordValidationService;

    @Mock
    private PasswordHistoryService passwordHistoryService;

    @InjectMocks
    private ChangePasswordUseCase useCase;

    private ChangePasswordRequestDto buildRequest(String old, String newPw, String confirm) {
        return ChangePasswordRequestDto.builder()
                .passwordActual(old).passwordNueva(newPw).passwordConfirmacion(confirm).build();
    }

    @Test
    @DisplayName("Debe cambiar contraseña exitosamente")
    void changePassword_Success() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity user = UsuarioEntity.builder().id(userId).clave("encoded").build();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("New1234!", "New1234!")).thenReturn(true);
        when(passwordValidationService.validate("New1234!")).thenReturn(
                PasswordValidationResultDto.builder().valid(true).errors(List.of()).strengthScore(100).build());
        when(passwordValidationService.isPasswordReused("New1234!", Optional.of(user))).thenReturn(false);
        when(passwordHistoryService.isSameAsCurrent("New1234!", user)).thenReturn(false);
        when(passwordHistoryService.isInHistory("New1234!", userId)).thenReturn(false);
        when(passwordValidationService.encodePassword("New1234!")).thenReturn("newEncoded");

        ChangePasswordResponseDto result = useCase.changePassword(userId, buildRequest("old", "New1234!", "New1234!"));

        assertThat(result.isSuccess()).isTrue();
        verify(passwordHistoryService).recordPreviousPassword(userId, "encoded");
        verify(usuarioRepository).save(user);
    }

    @Test
    @DisplayName("Debe fallar si usuario no existe")
    void changePassword_UserNotFound() {
        UUID userId = UUID.randomUUID();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.changePassword(userId, buildRequest("o", "n", "n")))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    @DisplayName("Debe fallar si contraseña actual es incorrecta")
    void changePassword_WrongOldPassword() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity user = UsuarioEntity.builder().id(userId).clave("encoded").build();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordValidationService.validateOldPassword("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> useCase.changePassword(userId, buildRequest("wrong", "new", "new")))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("Debe fallar si nueva contraseña y confirmación no coinciden")
    void changePassword_PasswordMismatch() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity user = UsuarioEntity.builder().id(userId).clave("encoded").build();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("new1", "new2")).thenReturn(false);

        assertThatThrownBy(() -> useCase.changePassword(userId, buildRequest("old", "new1", "new2")))
                .isInstanceOf(PasswordChangeException.class);
    }

    @Test
    @DisplayName("Debe fallar si nueva contraseña es débil")
    void changePassword_WeakPassword() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity user = UsuarioEntity.builder().id(userId).clave("encoded").build();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("weak", "weak")).thenReturn(true);
        when(passwordValidationService.validate("weak")).thenReturn(
                PasswordValidationResultDto.builder().valid(false).errors(List.of("too short")).strengthScore(10).build());

        assertThatThrownBy(() -> useCase.changePassword(userId, buildRequest("old", "weak", "weak")))
                .isInstanceOf(PasswordChangeException.class);
    }

    @Test
    @DisplayName("Debe fallar si contraseña es reutilizada")
    void changePassword_PasswordReused() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity user = UsuarioEntity.builder().id(userId).clave("encoded").build();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("Same1234!", "Same1234!")).thenReturn(true);
        when(passwordValidationService.validate("Same1234!")).thenReturn(
                PasswordValidationResultDto.builder().valid(true).errors(List.of()).strengthScore(100).build());
        when(passwordValidationService.isPasswordReused("Same1234!", Optional.of(user))).thenReturn(true);

        assertThatThrownBy(() -> useCase.changePassword(userId, buildRequest("old", "Same1234!", "Same1234!")))
                .isInstanceOf(PasswordChangeException.class);
    }

    @Test
    @DisplayName("Debe fallar si nueva contraseña es igual a la actual (mensaje BDD)")
    void changePassword_SameAsCurrentMessage() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity user = UsuarioEntity.builder().id(userId).clave("encoded").build();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("Same1234!", "Same1234!")).thenReturn(true);
        when(passwordValidationService.validate("Same1234!")).thenReturn(
                PasswordValidationResultDto.builder().valid(true).errors(List.of()).strengthScore(100).build());
        when(passwordHistoryService.isSameAsCurrent("Same1234!", user)).thenReturn(true);

        assertThatThrownBy(() -> useCase.changePassword(userId, buildRequest("old", "Same1234!", "Same1234!")))
                .isInstanceOf(PasswordChangeException.class)
                .hasMessageContaining("no puede ser igual a la actual");
    }

    @Test
    @DisplayName("Debe fallar si contraseña está en el historial de las últimas 3")
    void changePassword_PasswordInHistory() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity user = UsuarioEntity.builder().id(userId).clave("encoded").build();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("OldUsed123!", "OldUsed123!")).thenReturn(true);
        when(passwordValidationService.validate("OldUsed123!")).thenReturn(
                PasswordValidationResultDto.builder().valid(true).errors(List.of()).strengthScore(100).build());
        when(passwordHistoryService.isSameAsCurrent("OldUsed123!", user)).thenReturn(false);
        when(passwordHistoryService.isInHistory("OldUsed123!", userId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.changePassword(userId, buildRequest("old", "OldUsed123!", "OldUsed123!")))
                .isInstanceOf(PasswordChangeException.class)
                .hasMessageContaining("ya fue utilizada");
    }
}
