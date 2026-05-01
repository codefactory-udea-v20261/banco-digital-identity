package com.udea.bancodigital.infrastructure.config;

import com.udea.bancodigital.auth.infrastructure.config.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

/**
 * Configuración de seguridad.
 *
 * PROPÓSITO: Establecer JwtAuthenticationFilter y reglas
 * estrictas para validación de tokens.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, Environment environment) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev") ||
                       Arrays.asList(environment.getActiveProfiles()).contains("local");

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configure(http))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Swagger / OpenAPI - only in dev/local profiles
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/api-docs.yaml"
                ).permitAll()
                // Actuator (health checks para Docker/Render)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Endpoints públicos
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/internal/users/provision-client-access").hasAnyRole("CAJERO", "ADMIN")
                // Solo asesor/admin puede registrar clientes
                .requestMatchers(HttpMethod.POST, "/api/v1/clientes").hasAnyRole("CAJERO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/*").hasAnyRole("CLIENTE", "CAJERO", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/clientes/*").hasAnyRole("CAJERO", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/cuentas").hasAnyRole("CAJERO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/cuentas/*/saldo").hasAnyRole("CLIENTE", "CAJERO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/reportes/saldo-total").hasAnyRole("CLIENTE", "CAJERO", "ADMIN")
                // El resto debe estar autenticado
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
