let AUDITORIA_STATE = {
    auditorias: []
};

document.addEventListener('DOMContentLoaded', function() {
    if (!requireAuth()) return;
    
    loadUserInfo();
    loadAuditoria();
});

function loadUserInfo() {
    const user = Storage.getUser();
    if (user) {
        document.getElementById('userName').textContent = user.nombre;
    }
}

async function loadAuditoria() {
    try {
        const container = document.getElementById('auditoriaList');
        container.innerHTML = '<div class="loading">Cargando registros de auditoría...</div>';
        
        // Construir URL con filtros
        let url = buildUrl(API_CONFIG.ENDPOINTS.AUDITORIA);
        const params = new URLSearchParams();
        
        const operacion = document.getElementById('filterOperacion').value;
        const entidad = document.getElementById('filterEntidad').value;
        const fechaInicio = document.getElementById('filterFechaInicio').value;
        const fechaFin = document.getElementById('filterFechaFin').value;
        
        if (operacion) params.append('tipoOperacion', operacion);
        if (entidad) params.append('entidad', entidad);
        if (fechaInicio) params.append('fechaInicio', fechaInicio + 'T00:00:00');
        if (fechaFin) params.append('fechaFin', fechaFin + 'T23:59:59');
        
        if (params.toString()) {
            url += '?' + params.toString();
        }
        
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${Storage.getToken()}`
            }
        });
        
        if (response.ok) {
            AUDITORIA_STATE.auditorias = await response.json();
            renderAuditoria();
            updateStats();
        } else {
            throw new Error('Error al cargar auditoría');
        }
        
    } catch (error) {
        console.error('Error cargando auditoría:', error);
        document.getElementById('auditoriaList').innerHTML = 
            '<div class="loading">Error al cargar registros de auditoría</div>';
    }
}

function renderAuditoria() {
    const container = document.getElementById('auditoriaList');
    
    if (AUDITORIA_STATE.auditorias.length === 0) {
        container.innerHTML = '<div class="loading">No se encontraron registros de auditoría</div>';
        return;
    }
    
    container.innerHTML = AUDITORIA_STATE.auditorias.map(auditoria => `
        <div class="auditoria-item">
            <div class="auditoria-icon">
                ${getAuditoriaIcon(auditoria.tipoOperacion)}
            </div>
            <div class="auditoria-info">
                <h4>${auditoria.usuario?.nombre || 'Usuario'} - ${auditoria.tipoOperacion} ${auditoria.entidadAfectada}</h4>
                <p><strong>Entidad:</strong> ${auditoria.entidadAfectada} (ID: ${auditoria.idEntidadAfectada})</p>
                <p><strong>Fecha:</strong> ${new Date(auditoria.fechaHora).toLocaleString()}</p>
                ${auditoria.descripcion ? `<p><strong>Descripción:</strong> ${auditoria.descripcion}</p>` : ''}
                
                ${auditoria.valoresAntes || auditoria.valoresDespues ? `
                    <div class="auditoria-details">
                        <strong>Detalles del Cambio:</strong><br>
                        ${auditoria.valoresAntes ? `<span style="color: #ef4444;">Antes: ${formatJson(auditoria.valoresAntes)}</span><br>` : ''}
                        ${auditoria.valoresDespues ? `<span style="color: #10b981;">Después: ${formatJson(auditoria.valoresDespues)}</span>` : ''}
                    </div>
                ` : ''}
            </div>
            <span class="auditoria-badge badge-${auditoria.tipoOperacion.toLowerCase()}">
                ${auditoria.tipoOperacion}
            </span>
        </div>
    `).join('');
}

function getAuditoriaIcon(tipoOperacion) {
    switch (tipoOperacion) {
        case 'INSERT': return '➕';
        case 'UPDATE': return '✏️';
        case 'DELETE': return '🗑️';
        default: return '📝';
    }
}

function formatJson(jsonString) {
    try {
        const json = JSON.parse(jsonString);
        return JSON.stringify(json, null, 2);
    } catch {
        return jsonString;
    }
}

function updateStats() {
    const auditorias = AUDITORIA_STATE.auditorias;
    const hoy = new Date().toDateString();
    
    const total = auditorias.length;
    const hoyCount = auditorias.filter(a => 
        new Date(a.fechaHora).toDateString() === hoy
    ).length;
    const creaciones = auditorias.filter(a => a.tipoOperacion === 'INSERT').length;
    const actualizaciones = auditorias.filter(a => a.tipoOperacion === 'UPDATE').length;
    
    document.getElementById('totalRegistros').textContent = total;
    document.getElementById('registrosHoy').textContent = hoyCount;
    document.getElementById('operacionesCreacion').textContent = creaciones;
    document.getElementById('operacionesActualizacion').textContent = actualizaciones;
}

function updateAuditoriaFilterCount() {
    let count = 0;
    if (document.getElementById('filterOperacion').value) count++;
    if (document.getElementById('filterEntidad').value) count++;
    if (document.getElementById('filterFechaInicio').value) count++;
    if (document.getElementById('filterFechaFin').value) count++;
    
    const badge = document.getElementById('auditoriaFilterCount');
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline-flex';
    } else {
        badge.style.display = 'none';
    }
}

function clearAuditoriaFilters() {
    document.getElementById('filterOperacion').value = '';
    document.getElementById('filterEntidad').value = '';
    document.getElementById('filterFechaInicio').value = '';
    document.getElementById('filterFechaFin').value = '';
    updateAuditoriaFilterCount();
    loadAuditoria();
}

// Cargar auditoría cada 30 segundos
setInterval(loadAuditoria, 30000);