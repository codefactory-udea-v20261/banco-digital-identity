package com.udea.bancodigital.shared.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void testApiResponse() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("OK")
                .data("DATA")
                .build();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isEqualTo("DATA");

        assertThat(response.toString()).isNotBlank();
        
        ApiResponse<String> responseOk = ApiResponse.ok("DATA");
        assertThat(responseOk.isSuccess()).isTrue();
        
        ApiResponse<String> responseError = ApiResponse.error(null);
        assertThat(responseError.isSuccess()).isFalse();
    }
}
