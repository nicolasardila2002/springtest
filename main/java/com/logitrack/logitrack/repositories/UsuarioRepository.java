package com.logitrack.logitrack.repositories;

import com.logitrack.logitrack.entities.Usuario;
import com.logitrack.logitrack.enums.Rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Métodos básicos derivados (como en BodegaRepository)
    Optional<Usuario> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<Usuario> findByActivoTrue();
    List<Usuario> findByRol(Rol rol);

    // ✅ CORREGIDO: Mismo estilo que BodegaRepository - sin "and" en nombre
    @Query("SELECT u FROM Usuario u WHERE u.id = :id AND u.rol = 'ADMIN'")
    Optional<Usuario> findAdminById(@Param("id") Long id);

    // ✅ Mismo estilo: Query con parámetro nombrado
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.activo = true")
    List<Usuario> findByRolAndActivo(@Param("rol") Rol rol);

    // ✅ Similar a findByCapacidadGreaterThan
    List<Usuario> findByActivo(Boolean activo);
}