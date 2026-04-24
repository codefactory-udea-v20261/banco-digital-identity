package com.udea.bancodigital.auth.infrastructure.adapter.out;

import com.udea.bancodigital.auth.domain.model.Rol;
import com.udea.bancodigital.auth.domain.model.Usuario;
import com.udea.bancodigital.auth.infrastructure.entity.RolEntity;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.RolJpaRepository;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioRepositoryAdapter")
class UsuarioRepositoryAdapterTest {

    @Mock
    private UsuarioJpaRepository usuarioJpaRepository;

    @Mock
    private RolJpaRepository rolJpaRepository;

    @InjectMocks
    private UsuarioRepositoryAdapter usuarioRepositoryAdapter;

    @Test
    @DisplayName("Debe mapear multiples roles al consultar por correo")
    void debeMapearMultiplesRolesAlConsultarPorCorreo() {
        UUID usuarioId = UUID.randomUUID();
        UsuarioEntity entity = UsuarioEntity.builder()
                .id(usuarioId)
                .correo("multirol@test.com")
                .clave("hash")
                .activo(true)
                .mfaActivo(true)
                .roles(Set.of(
                        rolEntity((short) 1, "ADMIN"),
                        rolEntity((short) 4, "AUDITOR")
                ))
                .build();

        when(usuarioJpaRepository.findByCorreo("multirol@test.com")).thenReturn(Optional.of(entity));

        Optional<Usuario> usuario = usuarioRepositoryAdapter.findByEmail("multirol@test.com");

        assertThat(usuario).isPresent();
        assertThat(usuario.get().getRoles())
                .extracting(Rol::getNombre)
                .containsExactlyInAnyOrder("ADMIN", "AUDITOR");
    }

    @Test
    @DisplayName("Debe persistir todos los roles del usuario")
    void debePersistirTodosLosRolesDelUsuario() {
        ReflectionTestUtils.setField(usuarioRepositoryAdapter, "lockoutMinutes", 15L);
        UUID usuarioId = UUID.randomUUID();
        RolEntity adminEntity = rolEntity((short) 1, "ADMIN");
        RolEntity auditorEntity = rolEntity((short) 4, "AUDITOR");
        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .correo("admin.auditor@test.com")
                .clave("hash")
                .activo(true)
                .bloqueado(false)
                .intentosFallidos(0)
                .mfaActivo(true)
                .roles(Set.of(
                        rol((short) 1, "ADMIN"),
                        rol((short) 4, "AUDITOR")
                ))
                .build();

        when(rolJpaRepository.findAllById(anyIterable())).thenReturn(List.of(adminEntity, auditorEntity));
        when(usuarioJpaRepository.save(any(UsuarioEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario saved = usuarioRepositoryAdapter.save(usuario);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles())
                .extracting(RolEntity::getNombre)
                .containsExactlyInAnyOrder("ADMIN", "AUDITOR");
        assertThat(saved.getRoles())
                .extracting(Rol::getNombre)
                .containsExactlyInAnyOrder("ADMIN", "AUDITOR");
    }

    private Rol rol(short id, String nombre) {
        return Rol.builder()
                .id(id)
                .nombre(nombre)
                .build();
    }

    private RolEntity rolEntity(short id, String nombre) {
        return RolEntity.builder()
                .id(id)
                .nombre(nombre)
                .build();
    }
}
