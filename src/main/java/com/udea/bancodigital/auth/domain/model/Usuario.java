package com.udea.bancodigital.auth.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class Usuario {
    UUID id;
    UUID clienteId;
    String correo;
    String clave;
    boolean activo;
    boolean bloqueado;
    Integer intentosFallidos;
    String secretoMfa;
    boolean mfaActivo;
    Set<Rol> roles;
}
