package com.logitrack.logitrack.services;

import com.logitrack.logitrack.dto.RegistroUsuarioDTO;
import com.logitrack.logitrack.entities.Usuario;
import com.logitrack.logitrack.enums.Rol;
import com.logitrack.logitrack.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoderService passwordEncoderService;

    public UsuarioService(UsuarioRepository usuarioRepository, 
                         PasswordEncoderService passwordEncoderService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoderService = passwordEncoderService;
    }  // ✅ CORREGIDO: Cerramos el constructor

    public Usuario crearUsuario(RegistroUsuarioDTO registroDTO) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + registroDTO.getEmail());
        }

        // Cifrar password
        String passwordHash = passwordEncoderService.encodePassword(registroDTO.getPassword());

        // Crear y guardar usuario
        Usuario usuario = new Usuario(
            registroDTO.getNombre(),
            registroDTO.getEmail(),
            passwordHash,
            registroDTO.getRol()
        );

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNombre(usuarioActualizado.getNombre());
                    usuario.setEmail(usuarioActualizado.getEmail());
                    usuario.setRol(usuarioActualizado.getRol());
                    usuario.setActivo(usuarioActualizado.getActivo());
                    return usuarioRepository.save(usuario);
                })
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    public void desactivarUsuario(Long id) {
        usuarioRepository.findById(id)
                .ifPresent(usuario -> {
                    usuario.setActivo(false);
                    usuarioRepository.save(usuario);
                });
    }

    public void activarUsuario(Long id) {
        usuarioRepository.findById(id)
                .ifPresent(usuario -> {
                    usuario.setActivo(true);
                    usuarioRepository.save(usuario);
                });
    }

    public List<Usuario> obtenerAdministradores() {
        return usuarioRepository.findByRol(Rol.ADMIN);
    }

    public boolean esAdministrador(Long usuarioId) {
        return usuarioRepository.findAdminById(usuarioId).isPresent();
    }

    public long contarUsuariosActivos() {
        return usuarioRepository.findByActivoTrue().size();
    }
}