package com.udea.bancodigital.auth.infrastructure.adapter.out;

import com.udea.bancodigital.auth.domain.model.Permiso;
import com.udea.bancodigital.auth.domain.model.Rol;
import com.udea.bancodigital.auth.domain.model.Usuario;
import com.udea.bancodigital.auth.infrastructure.entity.PermisoEntity;
import com.udea.bancodigital.auth.infrastructure.entity.RolEntity;
import com.udea.bancodigital.auth.infrastructure.entity.UsuarioEntity;
import com.udea.bancodigital.auth.infrastructure.repository.RolJpaRepository;
import com.udea.bancodigital.auth.infrastructure.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioRepositoryAdapter - Extended")
class UsuarioRepositoryAdapterExtendedTest {
    @Mock
    private UsuarioJpaRepository jpaRepository;

    @Mock
    private RolJpaRepository rolJpaRepository;

    private UsuarioRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UsuarioRepositoryAdapter(jpaRepository, rolJpaRepository);
        ReflectionTestUtils.setField(adapter, "lockoutMinutes", 15L);
    }

    private UsuarioEntity buildEntity(UUID id, UUID clienteId) {
        PermisoEntity permiso = PermisoEntity.builder()
                .id((short) 1).nombre("PERM_READ").descripcion("Read").build();
        RolEntity rol = RolEntity.builder()
                .id((short) 1).nombre("CLIENTE").permisos(Set.of(permiso)).build();
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(id);
        entity.setClienteId(clienteId);
        entity.setCorreo("test@banco.com");
        entity.setClave("hash");
        entity.setActivo(true);
        entity.setBloqueado(false);
        entity.setIntentosFallidos((short) 0);
        entity.setMfaActivo(false);
        entity.setRoles(Set.of(rol));
        return entity;
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmailTest {

        @Test
        @DisplayName("Debe retornar Optional con usuario cuando el correo existe")
        void debeRetornarUsuarioCuandoCorreoExiste() {
            // Arrange
            UUID id = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();
            UsuarioEntity entity = buildEntity(id, clienteId);
            when(jpaRepository.findByCorreo("test@banco.com")).thenReturn(Optional.of(entity));

            // Act
            Optional<Usuario> resultado = adapter.findByEmail("test@banco.com");

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getCorreo()).isEqualTo("test@banco.com");
            assertThat(resultado.get().getId()).isEqualTo(id);
            assertThat(resultado.get().getClienteId()).isEqualTo(clienteId);
        }

        @Test
        @DisplayName("Debe retornar Optional vacío cuando el correo no existe")
        void debeRetornarVacioCuandoCorreoNoExiste() {
            // Arrange
            when(jpaRepository.findByCorreo("noexiste@banco.com")).thenReturn(Optional.empty());

            // Act
            Optional<Usuario> resultado = adapter.findByEmail("noexiste@banco.com");

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debe mapear roles y permisos correctamente")
        void debeMappearRolesYPermisos() {
            // Arrange
            UsuarioEntity entity = buildEntity(UUID.randomUUID(), UUID.randomUUID());
            when(jpaRepository.findByCorreo("test@banco.com")).thenReturn(Optional.of(entity));

            // Act
            Optional<Usuario> resultado = adapter.findByEmail("test@banco.com");

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getRoles()).hasSize(1);
            Rol rol = resultado.get().getRoles().iterator().next();
            assertThat(rol.getNombre()).isEqualTo("CLIENTE");
            assertThat(rol.getPermisos()).hasSize(1);
            Permiso permiso = rol.getPermisos().iterator().next();
            assertThat(permiso.getNombre()).isEqualTo("PERM_READ");
        }
    }

    @Nested
    @DisplayName("findByUsername()")
    class FindByUsernameTest {

        @Test
        @DisplayName("Debe delegar a findByEmail ya que username es el correo")
        void debeDelegarAFindByEmail() {
            // Arrange
            UsuarioEntity entity = buildEntity(UUID.randomUUID(), UUID.randomUUID());
            when(jpaRepository.findByCorreo("test@banco.com")).thenReturn(Optional.of(entity));

            // Act
            Optional<Usuario> resultado = adapter.findByUsername("test@banco.com");

            // Assert
            assertThat(resultado).isPresent();
            verify(jpaRepository).findByCorreo("test@banco.com");
        }
    }

    @Nested
    @DisplayName("existsByUsername()")
    class ExistsByUsernameTest {

        @Test
        @DisplayName("Debe retornar true cuando el correo existe")
        void debeRetornarTrueCuandoExiste() {
            // Arrange
            when(jpaRepository.existsByCorreo("test@banco.com")).thenReturn(true);

            // Act
            boolean existe = adapter.existsByUsername("test@banco.com");

            // Assert
            assertThat(existe).isTrue();
            verify(jpaRepository).existsByCorreo("test@banco.com");
        }

        @Test
        @DisplayName("Debe retornar false cuando el correo no existe")
        void debeRetornarFalseCuandoNoExiste() {
            // Arrange
            when(jpaRepository.existsByCorreo("nuevo@banco.com")).thenReturn(false);

            // Act
            boolean existe = adapter.existsByUsername("nuevo@banco.com");

            // Assert
            assertThat(existe).isFalse();
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("Debe guardar usuario y retornar el dominio mapeado")
        void debeGuardarYRetornarDominio() {
            // Arrange
            UUID id = UUID.randomUUID();
            UUID clienteId = UUID.randomUUID();

            RolEntity rolEntity = RolEntity.builder()
                    .id((short) 1).nombre("CLIENTE").permisos(Set.of()).build();
            Rol rol = Rol.builder().id((short) 1).nombre("CLIENTE").permisos(Set.of()).build();

            Usuario usuario = Usuario.builder()
                    .id(id)
                    .clienteId(clienteId)
                    .correo("nuevo@banco.com")
                    .clave("hash")
                    .activo(true)
                    .bloqueado(false)
                    .mfaActivo(false)
                    .roles(Set.of(rol))
                    .build();

            UsuarioEntity savedEntity = buildEntity(id, clienteId);
            when(rolJpaRepository.findAllById(List.of((short) 1))).thenReturn(List.of(rolEntity));
            when(jpaRepository.save(any(UsuarioEntity.class))).thenReturn(savedEntity);

            // Act
            Usuario resultado = adapter.save(usuario);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(id);
            verify(jpaRepository).save(any(UsuarioEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si algún rol no existe en base de datos")
        void debeLanzarExcepcionSiRolNoExiste() {
            // Arrange
            Rol rolInexistente = Rol.builder().id((short) 99).nombre("INEXISTENTE").permisos(Set.of()).build();
            Usuario usuario = Usuario.builder()
                    .id(UUID.randomUUID())
                    .correo("test@banco.com")
                    .clave("hash")
                    .activo(true)
                    .bloqueado(false)
                    .mfaActivo(false)
                    .roles(Set.of(rolInexistente))
                    .build();

            when(rolJpaRepository.findAllById(any())).thenReturn(List.of());

            // Act / Assert
            assertThatThrownBy(() -> adapter.save(usuario))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("roles no existen");
        }
    }

}
