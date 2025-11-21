package com.logitrack.logitrack.services;

import com.logitrack.logitrack.entities.AuditoriaCambios;
import com.logitrack.logitrack.entities.Usuario;
import com.logitrack.logitrack.enums.TipoOperacion;
import com.logitrack.logitrack.repositories.AuditoriaCambiosRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaCambiosRepository auditoriaRepository;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuditoriaService(AuditoriaCambiosRepository auditoriaRepository, 
                          AuthService authService) {
        this.auditoriaRepository = auditoriaRepository;
        this.authService = authService;
        this.objectMapper = new ObjectMapper();
    }

    // ✅ REGISTRAR CREACIÓN
    @Transactional
    public void registrarCreacion(Object entidad, String descripcion) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            String entidadNombre = entidad.getClass().getSimpleName();
            Long idEntidad = obtenerIdEntidad(entidad);
            
            String valoresDespues = convertirAJson(entidad);
            
            AuditoriaCambios auditoria = new AuditoriaCambios(
                usuario,
                TipoOperacion.INSERT,
                entidadNombre,
                idEntidad
            );
            auditoria.setValoresDespues(valoresDespues);
            auditoria.setDescripcion(descripcion);
            
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            // No lanzar excepción para no interrumpir el flujo principal
            System.err.println("Error al registrar auditoría de creación: " + e.getMessage());
        }
    }

    // ✅ REGISTRAR ACTUALIZACIÓN
    @Transactional
    public void registrarActualizacion(Object entidadAntes, Object entidadDespues, String descripcion) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            String entidadNombre = entidadDespues.getClass().getSimpleName();
            Long idEntidad = obtenerIdEntidad(entidadDespues);
            
            String valoresAntes = convertirAJson(entidadAntes);
            String valoresDespues = convertirAJson(entidadDespues);
            
            AuditoriaCambios auditoria = new AuditoriaCambios(
                usuario,
                TipoOperacion.UPDATE,
                entidadNombre,
                idEntidad
            );
            auditoria.setValoresAntes(valoresAntes);
            auditoria.setValoresDespues(valoresDespues);
            auditoria.setDescripcion(descripcion);
            
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría de actualización: " + e.getMessage());
        }
    }

    // ✅ REGISTRAR ELIMINACIÓN
    @Transactional
    public void registrarEliminacion(Object entidad, String descripcion) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            String entidadNombre = entidad.getClass().getSimpleName();
            Long idEntidad = obtenerIdEntidad(entidad);
            
            String valoresAntes = convertirAJson(entidad);
            
            AuditoriaCambios auditoria = new AuditoriaCambios(
                usuario,
                TipoOperacion.DELETE,
                entidadNombre,
                idEntidad
            );
            auditoria.setValoresAntes(valoresAntes);
            auditoria.setDescripcion(descripcion);
            
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría de eliminación: " + e.getMessage());
        }
    }

    // ✅ MÉTODO GENÉRICO PARA REGISTRAR CUALQUIER OPERACIÓN
    @Transactional
    public void registrarOperacion(TipoOperacion operacion, Object entidad, String descripcion) {
        try {
            Usuario usuario = obtenerUsuarioActual();
            String entidadNombre = entidad.getClass().getSimpleName();
            Long idEntidad = obtenerIdEntidad(entidad);
            
            String valoresJson = convertirAJson(entidad);
            
            AuditoriaCambios auditoria = new AuditoriaCambios(
                usuario,
                operacion,
                entidadNombre,
                idEntidad
            );
            
            if (operacion == TipoOperacion.INSERT) {
                auditoria.setValoresDespues(valoresJson);
            } else {
                auditoria.setValoresAntes(valoresJson);
            }
            
            auditoria.setDescripcion(descripcion);
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }

    // ✅ CONSULTAS PARA EL CONTROLADOR

    public List<AuditoriaCambios> obtenerTodasAuditorias() {
        return auditoriaRepository.findAll();
    }

    public List<AuditoriaCambios> obtenerAuditoriasPorUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioId(usuarioId);
    }

    public List<AuditoriaCambios> obtenerAuditoriasPorOperacion(TipoOperacion tipoOperacion) {
        return auditoriaRepository.findByTipoOperacion(tipoOperacion);
    }

    public List<AuditoriaCambios> obtenerAuditoriasPorEntidad(String entidad) {
        return auditoriaRepository.findByEntidadAfectada(entidad);
    }

    public List<AuditoriaCambios> obtenerAuditoriasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return auditoriaRepository.findByFechaHoraBetween(fechaInicio, fechaFin);
    }

    public List<AuditoriaCambios> obtenerAuditoriasPorUsuarioYFecha(Long usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return auditoriaRepository.findByUsuarioIdAndFechaHoraBetween(usuarioId, fechaInicio, fechaFin);
    }

    public List<AuditoriaCambios> obtenerAuditoriasPorEntidadYId(String entidad, Long idEntidad) {
        return auditoriaRepository.findByEntidadYId(entidad, idEntidad);
    }

    public List<AuditoriaCambios> obtenerUltimasAuditorias() {
        return auditoriaRepository.findUltimasAuditorias();
    }

    public List<AuditoriaCambios> obtenerAuditoriasDeHoy() {
        return auditoriaRepository.findAuditoriasDeHoy();
    }

    public List<Object[]> obtenerResumenOperacionesPorUsuario() {
        return auditoriaRepository.findResumenOperacionesPorUsuario();
    }

    public List<Object[]> obtenerEntidadesMasAuditadas() {
        return auditoriaRepository.findEntidadesMasAuditadas();
    }

    public List<AuditoriaCambios> buscarAuditoriasPorDescripcion(String texto) {
        return auditoriaRepository.findByDescripcionContaining(texto);
    }

    public AuditoriaCambios obtenerAuditoriaPorId(Long id) {
        return auditoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auditoría no encontrada con ID: " + id));
    }

    // ✅ MÉTODOS PRIVADOS AUXILIARES
    private Usuario obtenerUsuarioActual() {
        try {
            return authService.getUsuarioActual();
        } catch (Exception e) {
            // Usuario por defecto para operaciones del sistema
            Usuario usuarioSistema = new Usuario();
            usuarioSistema.setId(0L);
            usuarioSistema.setNombre("SISTEMA");
            usuarioSistema.setEmail("sistema@logitrack.com");
            return usuarioSistema;
        }
    }

    private Long obtenerIdEntidad(Object entidad) {
        try {
            // Usar reflexión para obtener el ID de la entidad
            var idField = entidad.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (Long) idField.get(entidad);
        } catch (Exception e) {
            return 0L; // ID por defecto si no se puede obtener
        }
    }

    private String convertirAJson(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (Exception e) {
            return "{\"error\": \"No se pudo serializar el objeto\"}";
        }
    }
}