package com.udea.bancodigital.auth.infrastructure.config;

import com.udea.bancodigital.auth.domain.port.out.TokenBlacklistPort;
import com.udea.bancodigital.shared.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenBlacklistPort tokenBlacklistPort;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, TokenBlacklistPort tokenBlacklistPort) {
        this.jwtProvider = jwtProvider;
        this.tokenBlacklistPort = tokenBlacklistPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtProvider.isTokenValid(jwt)) {
                String jti = jwtProvider.extractJti(jwt);

                if (tokenBlacklistPort.isRevoked(jti)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token ha sido revocado");
                    return;
                }

                String username = jwtProvider.extractUsername(jwt);
                Claims claims = jwtProvider.getClaims(jwt);
                List<String> roles = claims.get("roles", List.class);
                UUID userId = jwtProvider.extractUserId(jwt);
                UUID clienteId = jwtProvider.extractClienteId(jwt);

                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

                AuthenticatedUser principal = new AuthenticatedUser(userId, username, clienteId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("No se pudo autenticar al usuario", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
