package com.udea.bancodigital.shared.util;

/**
 * Variante de UseCase para operaciones que no reciben parámetros de entrada.
 * Ej: listar tipos de cuenta, obtener configuración global.
 *
 * @param <O> Tipo del objeto de salida
 */
@FunctionalInterface
public interface UseCaseNoInput<O> {
    O ejecutar();
}
