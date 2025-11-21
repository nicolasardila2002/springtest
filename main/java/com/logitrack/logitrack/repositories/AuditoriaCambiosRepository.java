package com.logitrack.logitrack.repositories;

import com.logitrack.logitrack.entities.AuditoriaCambios;
import com.logitrack.logitrack.enums.TipoOperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaCambiosRepository extends JpaRepository<AuditoriaCambios, Long> {
    
    // ✅ Buscar auditorías por usuario
    List<AuditoriaCambios> findByUsuarioId(Long usuarioId);
    
    // ✅ Buscar auditorías por tipo de operación
    List<AuditoriaCambios> findByTipoOperacion(TipoOperacion tipoOperacion);
    
    // ✅ Buscar auditorías por entidad afectada
    List<AuditoriaCambios> findByEntidadAfectada(String entidadAfectada);
    
    // ✅ Buscar auditorías por rango de fechas
    List<AuditoriaCambios> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // ✅ Buscar auditorías por usuario y rango de fechas
    List<AuditoriaCambios> findByUsuarioIdAndFechaHoraBetween(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);
    
    // ✅ Consulta personalizada: Auditorías de una entidad específica
    @Query("SELECT a FROM AuditoriaCambios a WHERE a.entidadAfectada = :entidad AND a.idEntidadAfectada = :idEntidad")
    List<AuditoriaCambios> findByEntidadYId(
        @Param("entidad") String entidad, 
        @Param("idEntidad") Long idEntidad
    );
    
    // ✅ Consulta personalizada: Últimas auditorías ordenadas por fecha
    @Query("SELECT a FROM AuditoriaCambios a ORDER BY a.fechaHora DESC")
    List<AuditoriaCambios> findUltimasAuditorias();
    
    // ✅ Consulta personalizada: Resumen de operaciones por usuario
    @Query("SELECT a.usuario.nombre, a.tipoOperacion, COUNT(a) " +
           "FROM AuditoriaCambios a " +
           "GROUP BY a.usuario.nombre, a.tipoOperacion " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findResumenOperacionesPorUsuario();
    
    // ✅ Consulta personalizada: Auditorías con descripción que contenga texto
    @Query("SELECT a FROM AuditoriaCambios a WHERE a.descripcion LIKE %:texto%")
    List<AuditoriaCambios> findByDescripcionContaining(@Param("texto") String texto);
    
    // ✅ Consulta personalizada: Entidades más auditadas
    @Query("SELECT a.entidadAfectada, COUNT(a) as total " +
           "FROM AuditoriaCambios a " +
           "GROUP BY a.entidadAfectada " +
           "ORDER BY total DESC")
    List<Object[]> findEntidadesMasAuditadas();
    
    // ✅ Consulta personalizada: Auditorías de hoy
    @Query("SELECT a FROM AuditoriaCambios a WHERE DATE(a.fechaHora) = CURRENT_DATE")
    List<AuditoriaCambios> findAuditoriasDeHoy();
}