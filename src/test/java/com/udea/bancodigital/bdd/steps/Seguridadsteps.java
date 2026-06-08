package com.udea.bancodigital.bdd.steps;

import com.udea.bancodigital.auth.application.dto.ChangePasswordRequestDto;
import com.udea.bancodigital.auth.application.dto.ChangePasswordResponseDto;
import com.udea.bancodigital.auth.application.dto.LoginRequestDto;
import com.udea.bancodigital.auth.application.dto.LoginResponseDto;
import com.udea.bancodigital.auth.application.dto.LogoutRequestDto;
import com.udea.bancodigital.auth.application.dto.PasswordValidationResultDto;
import com.udea.bancodigital.auth.application.usecase.AuthUseCase;
import com.udea.bancodigital.auth.application.usecase.ChangePasswordUseCase;
import com.udea.bancodigital.auth.domain.model.Rol;
import com.udea.bancodigital.auth.domain.model.Usuario;
import com.udea.bancodigital.auth.domain.port.out.JwtProviderPort;
import com.udea.bancodigital.auth.domain.port.out.PasswordEncoderPort;
import com.udea.bancodigital.auth.domain.port.out.TokenBlacklistPort;
import com.udea.bancodigital.auth.domain.port.out.UsuarioRepositoryPort;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import com.udea.bancodigital.auth.infrastructure.service.PasswordValidationService;
import com.udea.bancodigital.auth.infrastructure.service.PasswordHistoryService;
import com.udea.bancodigital.shared.exception.CredencialesInvalidasException;
import com.udea.bancodigital.shared.exception.InvalidPasswordException;
import com.udea.bancodigital.shared.exception.PasswordChangeException;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class Seguridadsteps {
    private UsuarioRepositoryPort usuarioRepositoryPort;
    private PasswordEncoderPort passwordEncoderPort;
    private JwtProviderPort jwtProviderPort;
    private TokenBlacklistPort tokenBlacklistPort;
    private UsuarioJpaRepository usuarioJpaRepository;
    private PasswordValidationService passwordValidationService;
    private PasswordHistoryService passwordHistoryService;

    private AuthUseCase authUseCase;
    private ChangePasswordUseCase changePasswordUseCase;

    private UUID usuarioId;
    private LoginResponseDto loginResult;
    private ChangePasswordResponseDto cambioResult;
    private Exception excepcion;
    private Usuario usuarioDominio;
    private UsuarioEntity usuarioEntity;

    // Estado pendiente para HU12: guardamos la request y la ejecutamos en @Then
    private ChangePasswordRequestDto pendingPwdRequest;

    @Before
    public void init() {
        usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
        passwordEncoderPort = mock(PasswordEncoderPort.class);
        jwtProviderPort = mock(JwtProviderPort.class);
        tokenBlacklistPort = mock(TokenBlacklistPort.class);
        usuarioJpaRepository = mock(UsuarioJpaRepository.class);
        passwordValidationService = mock(PasswordValidationService.class);
        passwordHistoryService = mock(PasswordHistoryService.class);

        authUseCase = new AuthUseCase(usuarioRepositoryPort, passwordEncoderPort, jwtProviderPort, tokenBlacklistPort);
        changePasswordUseCase = new ChangePasswordUseCase(usuarioJpaRepository, passwordValidationService,
                passwordHistoryService);

        usuarioId = UUID.randomUUID();
        excepcion = null;
        loginResult = null;
        cambioResult = null;
        pendingPwdRequest = null;

        // Por defecto historial no bloquea
        when(passwordHistoryService.isSameAsCurrent(anyString(), any())).thenReturn(false);
        when(passwordHistoryService.isInHistory(anyString(), any())).thenReturn(false);
    }

    private Usuario buildUsuario(boolean bloqueado, int intentos) {
        return Usuario.builder()
                .id(usuarioId)
                .correo("cliente@test.com")
                .clave("encoded")
                .activo(true)
                .bloqueado(bloqueado)
                .intentosFallidos(intentos)
                .roles(Set.of(Rol.builder().nombre("ROLE_CLIENTE").build()))
                .build();
    }

    private void catchEx(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            excepcion = e;
        }
    }

    // ── HU11 ─────────────────────────────────────────────────────────────────

    @Given("el usuario {string} existe con clave codificada y no esta bloqueado")
    public void usuarioExisteNoBloqueado(String correo) {
        usuarioDominio = buildUsuario(false, 0);
        when(usuarioRepositoryPort.findByEmail(correo)).thenReturn(Optional.of(usuarioDominio));
        when(passwordEncoderPort.matches("Test1234!", "encoded")).thenReturn(true);
        when(jwtProviderPort.generateToken(usuarioDominio)).thenReturn("jwt-token");
        when(jwtProviderPort.generateRefreshToken(usuarioDominio)).thenReturn("refresh-token");
    }

    @When("el cliente hace login con correo {string} y clave {string}")
    public void clienteHaceLogin(String correo, String clave) {
        LoginRequestDto req = LoginRequestDto.builder().correo(correo).clave(clave).build();
        catchEx(() -> loginResult = authUseCase.login(req));
    }

    @Then("el sistema retorna un token JWT valido")
    public void retornaTokenJWT() {
        assertThat(loginResult).isNotNull();
        assertThat(loginResult.getToken()).isEqualTo("jwt-token");
    }

    @And("no se guarda ninguna modificacion sobre el usuario")
    public void noSeGuardaModificacion() {
        verify(usuarioRepositoryPort, never()).save(any());
        verify(usuarioJpaRepository, never()).save(any());
    }

    @Given("el usuario {string} existe con {int} intento fallido previo")
    public void usuarioConUnIntentoFallido(String correo, int intentos) {
        // Con intentos < maxFailedAttempts (3), el usuario NO está bloqueado
        usuarioDominio = buildUsuario(false, intentos);
        when(usuarioRepositoryPort.findByEmail(correo)).thenReturn(Optional.of(usuarioDominio));
        when(passwordEncoderPort.matches("mala", "encoded")).thenReturn(false);
        when(usuarioRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Then("el sistema lanza CredencialesInvalidasException")
    public void lanzaCredencialesInvalidasException() {
        assertThat(excepcion).isInstanceOf(CredencialesInvalidasException.class);
    }

    @Then("el sistema lanza CuentaBloqueadaException")
    public void lanzaCuentaBloqueadaException() {
        assertThat(excepcion).isInstanceOf(com.udea.bancodigital.shared.exception.CuentaBloqueadaException.class);
    }

    @And("los intentos fallidos del usuario quedan en {int}")
    public void intentosFallidosQuedanEn(int esperado) {
        ArgumentCaptor<Usuario> cap = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositoryPort).save(cap.capture());
        assertThat(cap.getValue().getIntentosFallidos()).isEqualTo(esperado);
    }

    @Given("el usuario {string} existe con {int} intentos fallidos previos")
    public void usuarioConVariosIntentosFallidos(String correo, int intentos) {
        // Con intentos = 2 (maxFailedAttempts - 1), el usuario NO está bloqueado
        // todavía
        // Al fallar una vez más (intento 3 >= 3) queda bloqueado
        usuarioDominio = buildUsuario(false, intentos);
        when(usuarioRepositoryPort.findByEmail(correo)).thenReturn(Optional.of(usuarioDominio));
        when(passwordEncoderPort.matches("mala", "encoded")).thenReturn(false);
        when(usuarioRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @And("la cuenta del usuario queda bloqueada")
    public void cuentaUsuarioBloqueada() {
        ArgumentCaptor<Usuario> cap = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositoryPort).save(cap.capture());
        assertThat(cap.getValue().isBloqueado()).isTrue();
    }

    @Given("el usuario tiene una sesion activa con token {string} con JTI {string} y expiracion futura")
    public void sesionActivaConToken(String token, String jti) {
        when(jwtProviderPort.extractJti(token)).thenReturn(jti);
        when(jwtProviderPort.extractExpiration(token))
                .thenReturn(Date.from(Instant.now().plusSeconds(3600)));
        when(jwtProviderPort.extractUserId(token)).thenReturn(usuarioId);
    }

    @When("el cliente hace logout con ese token")
    public void clienteHaceLogout() {
        LogoutRequestDto req = LogoutRequestDto.builder().token("jwt-token").build();
        authUseCase.logout(req);
    }

    @Then("el token queda registrado en la blacklist con JTI {string}")
    public void tokenEnBlacklist(String jti) {
        verify(tokenBlacklistPort).revoke(eq(jti), eq(usuarioId), any(Instant.class));
    }

    // ── HU12 ─────────────────────────────────────────────────────────────────
    // IMPORTANTE: el @When solo guarda la request.
    // Los @And configuran los mocks.
    // El @Then ejecuta el use case con los mocks ya listos y verifica el resultado.

    @Given("el usuario existe con clave codificada {string}")
    public void usuarioExisteConClave(String clave) {
        usuarioEntity = UsuarioEntity.builder().id(usuarioId).clave(clave).build();
        when(usuarioJpaRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioEntity));
    }

    @When("cambia la contrasena con actual {string} nueva {string} confirmacion {string}")
    public void cambiaContrasena(String old, String nw, String confirm) {
        // Solo guardar la request — los mocks se configuran en el @And siguiente
        pendingPwdRequest = ChangePasswordRequestDto.builder()
                .passwordActual(old)
                .passwordNueva(nw)
                .passwordConfirmacion(confirm)
                .build();
    }

    @And("la contrasena actual es correcta y la nueva es valida y no reutilizada")
    public void contrasenaActualCorrectaYNuevaValida() {
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("New1234!", "New1234!")).thenReturn(true);
        when(passwordValidationService.validate("New1234!")).thenReturn(
                PasswordValidationResultDto.builder().valid(true).errors(List.of()).strengthScore(100).build());
        when(passwordHistoryService.isSameAsCurrent("New1234!", usuarioEntity)).thenReturn(false);
        when(passwordHistoryService.isInHistory("New1234!", usuarioId)).thenReturn(false);
        when(passwordValidationService.isPasswordReused("New1234!", Optional.of(usuarioEntity))).thenReturn(false);
        when(passwordValidationService.encodePassword("New1234!")).thenReturn("newEncoded");
        // Ejecutar ahora que los mocks están listos
        catchEx(() -> cambioResult = changePasswordUseCase.changePassword(usuarioId, pendingPwdRequest));
    }

    @Then("el resultado indica success true")
    public void resultadoSuccessTrue() {
        assertThat(cambioResult).isNotNull();
        assertThat(cambioResult.isSuccess()).isTrue();
    }

    @And("la nueva clave queda guardada en la base de datos")
    public void nuevaClaveGuardada() {
        verify(usuarioJpaRepository).save(argThat(u -> u.getClave().equals("newEncoded")));
    }

    @And("la contrasena actual no coincide con la almacenada")
    public void contrasenaActualNoCoincide() {
        when(passwordValidationService.validateOldPassword(anyString(), eq("encoded"))).thenReturn(false);
        // Ejecutar ahora
        catchEx(() -> cambioResult = changePasswordUseCase.changePassword(usuarioId, pendingPwdRequest));
    }

    @Then("el sistema lanza InvalidPasswordException")
    public void lanzaInvalidPasswordException() {
        assertThat(excepcion).isInstanceOf(InvalidPasswordException.class);
    }

    @And("la contrasena actual es correcta y la nueva coincide y cumple politica pero esta reutilizada")
    public void contrasenaReutilizada() {
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("Same1234!", "Same1234!")).thenReturn(true);
        when(passwordValidationService.validate("Same1234!")).thenReturn(
                PasswordValidationResultDto.builder().valid(true).errors(List.of()).strengthScore(100).build());
        when(passwordHistoryService.isSameAsCurrent("Same1234!", usuarioEntity)).thenReturn(true);
        // Ejecutar
        catchEx(() -> cambioResult = changePasswordUseCase.changePassword(usuarioId, pendingPwdRequest));
    }

    @Then("el sistema lanza PasswordChangeException")
    public void lanzaPasswordChangeException() {
        assertThat(excepcion).isInstanceOf(PasswordChangeException.class);
    }

    @And("la contrasena actual es correcta pero nueva y confirmacion no coinciden")
    public void confirmacionNoCoincide() {
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("new1", "new2")).thenReturn(false);
        // Ejecutar
        catchEx(() -> cambioResult = changePasswordUseCase.changePassword(usuarioId, pendingPwdRequest));
    }

    @And("la contrasena actual es correcta y las nuevas coinciden pero no cumplen la politica")
    public void contrasenaNuevaNoCumplePolitica() {
        when(passwordValidationService.validateOldPassword("old", "encoded")).thenReturn(true);
        when(passwordValidationService.passwordsMatch("weak", "weak")).thenReturn(true);
        when(passwordValidationService.validate("weak")).thenReturn(
                PasswordValidationResultDto.builder().valid(false)
                        .errors(List.of("too short")).strengthScore(10).build());
        // Ejecutar
        catchEx(() -> cambioResult = changePasswordUseCase.changePassword(usuarioId, pendingPwdRequest));
    }

}
