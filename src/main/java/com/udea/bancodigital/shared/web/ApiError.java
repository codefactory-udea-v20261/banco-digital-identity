package com.udea.bancodigital.shared.web;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Estructura de error estandarizada.
 * Incluye traceId para trazabilidad end-to-end.
 */
@Getter
@Builder
public class ApiError {
    private final String errorCode;
    private final String message;
    private final List<String> details;
    private final String traceId;
    private final int httpStatus;
}
