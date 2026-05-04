package com.udea.bancodigital.auth.infrastructure.config;

import com.udea.bancodigital.auth.domain.port.out.TokenBlacklistPort;
import com.udea.bancodigital.shared.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenBlacklistPort tokenBlacklistPort;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe autenticar con token válido")
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String token = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtProvider.isTokenValid(token)).thenReturn(true);
        when(jwtProvider.extractJti(token)).thenReturn("jti-123");
        when(tokenBlacklistPort.isRevoked("jti-123")).thenReturn(false);
        when(jwtProvider.extractUsername(token)).thenReturn("user@test.com");
        when(jwtProvider.getClaims(token)).thenReturn(claims);
        when(claims.get("roles", List.class)).thenReturn(List.of("ADMIN"));
        when(jwtProvider.extractUserId(token)).thenReturn(userId);
        when(jwtProvider.extractClienteId(token)).thenReturn(clienteId);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(AuthenticatedUser.class);
        AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();
        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.username()).isEqualTo("user@test.com");
        assertThat(principal.clienteId()).isEqualTo(clienteId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe rechazar token revocado")
    void doFilterInternal_WithRevokedToken_ShouldSendError() throws Exception {
        String token = "revoked.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtProvider.isTokenValid(token)).thenReturn(true);
        when(jwtProvider.extractJti(token)).thenReturn("jti-revoked");
        when(tokenBlacklistPort.isRevoked("jti-revoked")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token ha sido revocado");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Debe continuar sin autenticación cuando no hay header")
    void doFilterInternal_WithNoHeader_ShouldContinue() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe continuar sin autenticación cuando header no es Bearer")
    void doFilterInternal_WithNonBearerHeader_ShouldContinue() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe continuar cuando token es inválido")
    void doFilterInternal_WithInvalidToken_ShouldContinue() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(jwtProvider.isTokenValid("invalid.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe manejar excepción sin romper el filtro")
    void doFilterInternal_WhenExceptionThrown_ShouldContinue() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer crash.token");
        when(jwtProvider.isTokenValid("crash.token")).thenThrow(new RuntimeException("JWT parse failure"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
