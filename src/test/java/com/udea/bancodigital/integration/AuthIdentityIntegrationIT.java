package com.udea.bancodigital.integration;

import com.udea.bancodigital.auth.application.dto.LoginRequestDto;
import com.udea.bancodigital.auth.application.dto.LoginResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Disabled;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("it")
@Disabled("Requires Docker, skipping in CI without Docker environment")
class AuthIdentityIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("banco_digital_identity_it")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRejectInvalidCredentials() {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .correo("invalid@test.com")
                .clave("wrongpassword")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> entity = new HttpEntity<>(loginRequest, headers);

        var response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                entity,
                LoginResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .correo("not-an-email")
                .clave("password123")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> entity = new HttpEntity<>(loginRequest, headers);

        var response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                entity,
                LoginResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldRejectMissingCredentials() {
        LoginRequestDto loginRequest = new LoginRequestDto();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> entity = new HttpEntity<>(loginRequest, headers);

        var response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                entity,
                LoginResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}