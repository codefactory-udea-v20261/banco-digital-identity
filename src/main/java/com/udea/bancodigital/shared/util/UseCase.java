package com.udea.bancodigital.shared.util;

/**
 * Interfaz genérica para casos de uso con un parámetro de entrada y uno de salida.
 * Todos los casos de uso del proyecto DEBEN implementar esta interfaz o su variante.
 *
 * @param <I> Tipo del objeto de entrada (RequestDto)
 * @param <O> Tipo del objeto de salida (ResponseDto)
 *
 * Ejemplo:
 *   public class CrearClienteUseCase implements UseCase<CrearClienteRequestDto, ClienteResponseDto> {
 *       public ClienteResponseDto ejecutar(CrearClienteRequestDto request) { ... }
 *   }
 */
@FunctionalInterface
public interface UseCase<I, O> {
    O ejecutar(I input);
}
