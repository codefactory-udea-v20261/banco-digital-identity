package com.udea.bancodigital.auth.domain.port.out;

import com.udea.bancodigital.auth.domain.model.Usuario;

import java.util.Optional;

/**
 * Puerto de salida para acceso a repositorio de usuarios.
 * 
 * RESPONSABILIDAD:
 * - Buscar usuarios para autenticación
 * - Verificar existencia de usuarios
 * - Guardar nuevos usuarios (registro)
 */
public interface UsuarioRepositoryPort {
    
    /**
     * Busca un usuario por su username.
     *
     * @param username Username del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByUsername(String username);
    
    /**
     * Busca un usuario por su email.
     *
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el username dado.
     *
     * @param username Username a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsername(String username);
    
    /**
     * Guarda un usuario (nuevo o actualizado).
     *
     * @param usuario Usuario a guardar
     * @return Usuario guardado
     */
    Usuario save(Usuario usuario);
}

