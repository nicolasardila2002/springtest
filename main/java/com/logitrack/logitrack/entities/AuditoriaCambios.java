package com.logitrack.logitrack.entities;

import com.logitrack.logitrack.enums.TipoOperacion;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_cambios")
public class AuditoriaCambios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;
    
    @Column(name = "entidad_afectada", nullable = false, length = 100)
    private String entidadAfectada;
    
    @Column(name = "id_entidad_afectada", nullable = false)
    private Long idEntidadAfectada;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_antes", columnDefinition = "json")
    private String valoresAntes;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_despues", columnDefinition = "json")
    private String valoresDespues;
    
    @Column(length = 500)
    private String descripcion;
    
    // Constructores
    public AuditoriaCambios() {}
    
    public AuditoriaCambios(Usuario usuario, TipoOperacion tipoOperacion, 
                          String entidadAfectada, Long idEntidadAfectada) {
        this.usuario = usuario;
        this.tipoOperacion = tipoOperacion;
        this.entidadAfectada = entidadAfectada;
        this.idEntidadAfectada = idEntidadAfectada;
        this.fechaHora = LocalDateTime.now();
    }
    
    public AuditoriaCambios(Usuario usuario, TipoOperacion tipoOperacion,
                          String entidadAfectada, Long idEntidadAfectada,
                          String valoresAntes, String valoresDespues, String descripcion) {
        this(usuario, tipoOperacion, entidadAfectada, idEntidadAfectada);
        this.valoresAntes = valoresAntes;
        this.valoresDespues = valoresDespues;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public TipoOperacion getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(TipoOperacion tipoOperacion) { this.tipoOperacion = tipoOperacion; }
    
    public String getEntidadAfectada() { return entidadAfectada; }
    public void setEntidadAfectada(String entidadAfectada) { this.entidadAfectada = entidadAfectada; }
    
    public Long getIdEntidadAfectada() { return idEntidadAfectada; }
    public void setIdEntidadAfectada(Long idEntidadAfectada) { this.idEntidadAfectada = idEntidadAfectada; }
    
    public String getValoresAntes() { return valoresAntes; }
    public void setValoresAntes(String valoresAntes) { this.valoresAntes = valoresAntes; }
    
    public String getValoresDespues() { return valoresDespues; }
    public void setValoresDespues(String valoresDespues) { this.valoresDespues = valoresDespues; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    // Métodos helper para JSON
    public void setValoresAntesJson(Object objeto) {
        this.valoresAntes = convertirAJson(objeto);
    }
    
    public void setValoresDespuesJson(Object objeto) {
        this.valoresDespues = convertirAJson(objeto);
    }
    
    private String convertirAJson(Object objeto) {
        // En un servicio real, usarías Jackson/Gson para convertir a JSON
        // Por ahora retornamos string simple
        return objeto != null ? objeto.toString() : null;
    }
}