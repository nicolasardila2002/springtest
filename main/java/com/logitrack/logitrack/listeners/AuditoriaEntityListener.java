package com.logitrack.logitrack.listeners;

import com.logitrack.logitrack.services.AuditoriaService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PostRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaEntityListener {

    private static AuditoriaService auditoriaService;

    @Autowired
    public void setAuditoriaService(AuditoriaService auditoriaService) {
        AuditoriaEntityListener.auditoriaService = auditoriaService;
    }

    @PostPersist
    public void afterInsert(Object entity) {
        if (auditoriaService != null) {
            auditoriaService.registrarCreacion(entity, "Inserción automática vía JPA Listener");
        }
    }

    @PostUpdate
    public void afterUpdate(Object entity) {
        if (auditoriaService != null) {
            // Para updates necesitaríamos capturar el estado anterior
            // Esto se maneja mejor con AOP o en el servicio
            auditoriaService.registrarOperacion(
                com.logitrack.logitrack.enums.TipoOperacion.UPDATE,
                entity,
                "Actualización automática vía JPA Listener"
            );
        }
    }

    @PostRemove
    public void afterDelete(Object entity) {
        if (auditoriaService != null) {
            auditoriaService.registrarEliminacion(entity, "Eliminación automática vía JPA Listener");
        }
    }
}