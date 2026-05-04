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


/**
 * Configuración de seguridad.
 *
 * PROPÓSITO: Establecer JwtAuthenticationFilter y reglas
 * estrictas para validación de tokens.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String PERM_MANAGE_CLIENTS = PERM_MANAGE_CLIENTS;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, Environment environment) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

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
                .requestMatchers("/api/v1/auth/validate-token").permitAll()
                .requestMatchers("/api/v1/auth/refresh").permitAll()
                .requestMatchers("/api/v1/internal/users/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                // Solo asesor/admin puede registrar clientes
                .requestMatchers(HttpMethod.POST, "/api/v1/clientes").hasAuthority(PERM_MANAGE_CLIENTS)
                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/*").hasAnyAuthority("PERM_READ_OWN_PROFILE", PERM_MANAGE_CLIENTS)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/clientes/*").hasAuthority(PERM_MANAGE_CLIENTS)
                .requestMatchers(HttpMethod.POST, "/api/v1/cuentas").hasAuthority("PERM_CREATE_ACCOUNTS")
                .requestMatchers(HttpMethod.GET, "/api/v1/cuentas/*/saldo").hasAnyAuthority("PERM_READ_OWN_BALANCE", PERM_MANAGE_CLIENTS)
                .requestMatchers(HttpMethod.GET, "/api/v1/reportes/saldo-total").hasAnyAuthority("PERM_GENERATE_OWN_REPORTS", "PERM_GENERATE_REPORTS")
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
