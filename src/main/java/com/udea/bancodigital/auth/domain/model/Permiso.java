package com.udea.bancodigital.auth.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Permiso {
    Short id;
    String nombre;
    String descripcion;
}