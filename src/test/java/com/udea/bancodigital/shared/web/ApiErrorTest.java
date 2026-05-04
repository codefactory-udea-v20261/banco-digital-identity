package com.udea.bancodigital.shared.web;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorTest {

    @Test
    void shouldBuildApiError() {
        ApiError error = ApiError.builder()
                .errorCode("TEST_ERROR")
                .message("Something went wrong")
                .details(java.util.List.of("detail1", "detail2"))
                .traceId("ABC12345")
                .httpStatus(400)
                .build();

        assertThat(error.getErrorCode()).isEqualTo("TEST_ERROR");
        assertThat(error.getMessage()).isEqualTo("Something went wrong");
        assertThat(error.getDetails()).hasSize(2);
        assertThat(error.getTraceId()).isEqualTo("ABC12345");
        assertThat(error.getHttpStatus()).isEqualTo(400);
    }

    @Test
    void shouldBuildMinimalApiError() {
        ApiError error = ApiError.builder()
                .errorCode("MINIMAL")
                .message("msg")
                .httpStatus(500)
                .build();

        assertThat(error.getDetails()).isNull();
        assertThat(error.getTraceId()).isNull();
    }
}
