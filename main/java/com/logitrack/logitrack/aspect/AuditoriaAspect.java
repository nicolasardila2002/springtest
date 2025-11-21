package com.logitrack.logitrack.aspect;

import com.logitrack.logitrack.annotations.Auditar;
import com.logitrack.logitrack.services.AuditoriaService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class AuditoriaAspect {

    private final AuditoriaService auditoriaService;
    private final ThreadLocal<Map<String, Object>> estadoAnterior = new ThreadLocal<>();

    public AuditoriaAspect(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Before("@annotation(auditar)")
    public void capturarEstadoAnterior(JoinPoint joinPoint, Auditar auditar) {
        try {
            // Capturar estado anterior para updates
            if (auditar.operacion() == com.logitrack.logitrack.enums.TipoOperacion.UPDATE) {
                Object entidad = joinPoint.getArgs()[0]; // Asumiendo que el primer parámetro es la entidad
                Map<String, Object> estado = new HashMap<>();
                estado.put("entidad", entidad);
                estadoAnterior.set(estado);
            }
        } catch (Exception e) {
            System.err.println("Error al capturar estado anterior: " + e.getMessage());
        }
    }

    @AfterReturning("@annotation(auditar)")
    public void registrarAuditoria(JoinPoint joinPoint, Auditar auditar) {
        try {
            Object entidad = joinPoint.getArgs()[0]; // Primer parámetro es la entidad
            
            switch (auditar.operacion()) {
                case INSERT:
                    auditoriaService.registrarCreacion(entidad, auditar.descripcion());
                    break;
                case UPDATE:
                    Map<String, Object> estado = estadoAnterior.get();
                    if (estado != null) {
                        Object entidadAntes = estado.get("entidad");
                        auditoriaService.registrarActualizacion(entidadAntes, entidad, auditar.descripcion());
                        estadoAnterior.remove();
                    }
                    break;
                case DELETE:
                    auditoriaService.registrarEliminacion(entidad, auditar.descripcion());
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error en aspecto de auditoría: " + e.getMessage());
        }
    }
}