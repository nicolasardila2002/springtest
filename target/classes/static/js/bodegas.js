let BODEGAS_STATE = {
    bodegas: [],
    usuarios: [],
    editingBodega: null
};

const debouncedFilterBodegas = debounce(filterBodegas, 300);

document.addEventListener('DOMContentLoaded', function() {
    if (!requireAuth()) return;
    
    loadUserInfo();
    loadBodegas();
    // Antes cargábamos una lista de usuarios; ahora se ingresará el ID manualmente
});

function loadUserInfo() {
    const user = Storage.getUser();
    if (user) {
        document.getElementById('userName').textContent = user.nombre;
    }
}

async function loadBodegas() {
    try {
        const response = await fetch(buildUrl(API_CONFIG.ENDPOINTS.BODEGAS), {
            headers: {
                'Authorization': `Bearer ${Storage.getToken()}`
            }
        });
        
        if (response.ok) {
            BODEGAS_STATE.bodegas = await response.json();
            renderBodegas();
        } else {
            throw new Error('Error al cargar bodegas');
        }
    } catch (error) {
        console.error('Error:', error);
        document.getElementById('bodegasList').innerHTML = 
            '<div class="loading">Error al cargar bodegas</div>';
    }
}

async function loadUsuarios() {
    // Mantener la función para compatibilidad futura. Actualmente no
    // poblamos un select; el formulario pedirá ingresar el ID del encargado.
    BODEGAS_STATE.usuarios = [];
}

function renderBodegas(bodegas = BODEGAS_STATE.bodegas) {
    const container = document.getElementById('bodegasList');
    
    if (bodegas.length === 0) {
        container.innerHTML = '<div class="loading">No se encontraron bodegas</div>';
        return;
    }
    
    container.innerHTML = bodegas.map(bodega => `
        <div class="bodega-card">
            <div class="bodega-header">
                <div>
                    <div class="bodega-name">${bodega.nombre}</div>
                    <span class="bodega-status ${bodega.activo ? 'status-active' : 'status-inactive'}">
                        ${bodega.activo ? 'Activa' : 'Inactiva'}
                    </span>
                </div>
            </div>
            <div class="bodega-info">
                <p><strong>Ubicación:</strong> ${bodega.ubicacion || 'No especificada'}</p>
                <p><strong>Capacidad:</strong> ${bodega.capacidad ? bodega.capacidad + ' unidades' : 'No especificada'}</p>
                <p><strong>Encargado:</strong> ${bodega.encargado?.nombre || 'No asignado'}</p>
                <p><strong>Creada:</strong> ${new Date(bodega.createdAt).toLocaleDateString()}</p>
            </div>
            <div class="bodega-actions">
                <button class="btn btn-primary btn-sm" onclick="editBodega(${bodega.id})">Editar</button>
                <button class="btn btn-secondary btn-sm" onclick="toggleBodegaStatus(${bodega.id}, ${!bodega.activo})">
                    ${bodega.activo ? 'Desactivar' : 'Activar'}
                </button>
            </div>
        </div>
    `).join('');
}

function filterBodegas() {
    const search = document.getElementById('searchBodega').value.toLowerCase();
    const estado = document.getElementById('filterActivo').value;
    
    let filtered = BODEGAS_STATE.bodegas;
    
    if (search) {
        filtered = filtered.filter(bodega => 
            bodega.nombre.toLowerCase().includes(search)
        );
    }
    
    if (estado !== '') {
        filtered = filtered.filter(bodega => 
            bodega.activo === (estado === 'true')
        );
    }
    
    renderBodegas(filtered);
}

function showModal(mode, bodega = null) {
    BODEGAS_STATE.editingBodega = bodega;
    const modal = document.getElementById('bodegaModal');
    const title = document.getElementById('modalTitle');
    
    if (mode === 'create') {
        title.textContent = 'Nueva Bodega';
        document.getElementById('bodegaForm').reset();
        document.getElementById('activo').checked = true;
        const encargadoNameEl = document.getElementById('encargadoName');
        if (encargadoNameEl) encargadoNameEl.textContent = '';
    } else {
        title.textContent = 'Editar Bodega';
        fillFormWithBodegaData(bodega);
    }
    
    modal.style.display = 'block';
}

function closeModal() {
    document.getElementById('bodegaModal').style.display = 'none';
    BODEGAS_STATE.editingBodega = null;
}

function fillFormWithBodegaData(bodega) {
    document.getElementById('nombre').value = bodega.nombre || '';
    document.getElementById('ubicacion').value = bodega.ubicacion || '';
    document.getElementById('capacidad').value = bodega.capacidad || '';
    document.getElementById('encargadoId').value = bodega.encargado?.id || '';
    const encargadoNameEl = document.getElementById('encargadoName');
    if (encargadoNameEl) {
        encargadoNameEl.textContent = bodega.encargado?.nombre ? `- ${bodega.encargado.nombre}` : '';
    }
    document.getElementById('activo').checked = bodega.activo !== false;
}

document.getElementById('bodegaForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    const bodegaData = {
        nombre: formData.get('nombre'),
        ubicacion: formData.get('ubicacion'),
        capacidad: formData.get('capacidad') ? parseInt(formData.get('capacidad')) : null,
        encargadoId: parseInt(formData.get('encargadoId')),
        activo: formData.get('activo') === 'on'
    };
    
    try {
        let response;
        
        if (BODEGAS_STATE.editingBodega) {
            // Editar bodega existente
            response = await fetch(buildUrl(`${API_CONFIG.ENDPOINTS.BODEGAS}/{id}`, {
                id: BODEGAS_STATE.editingBodega.id
            }), {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${Storage.getToken()}`
                },
                body: JSON.stringify(bodegaData)
            });
        } else {
            // Crear nueva bodega
            response = await fetch(buildUrl(API_CONFIG.ENDPOINTS.BODEGAS), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${Storage.getToken()}`
                },
                body: JSON.stringify(bodegaData)
            });
        }
        
        if (response.ok) {
            showNotification(
                BODEGAS_STATE.editingBodega ? 'Bodega actualizada' : 'Bodega creada', 
                'success'
            );
            closeModal();
            loadBodegas();
        } else {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Error al guardar bodega');
        }
        
    } catch (error) {
        console.error('Error:', error);
        showNotification(error.message, 'error');
    }
});

async function editBodega(id) {
    const bodega = BODEGAS_STATE.bodegas.find(b => b.id === id);
    if (bodega) {
        showModal('edit', bodega);
    }
}

async function toggleBodegaStatus(id, newStatus) {
    try {
        const bodega = BODEGAS_STATE.bodegas.find(b => b.id === id);
        if (!bodega) return;
        
        const response = await fetch(buildUrl(`${API_CONFIG.ENDPOINTS.BODEGAS}/{id}`, { id }), {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${Storage.getToken()}`
            },
            body: JSON.stringify({
                ...bodega,
                activo: newStatus
            })
        });
        
        if (response.ok) {
            showNotification(
                `Bodega ${newStatus ? 'activada' : 'desactivada'}`,
                'success'
            );
            loadBodegas();
        } else {
            throw new Error('Error al cambiar estado');
        }
        
    } catch (error) {
        console.error('Error:', error);
        showNotification('Error al cambiar estado', 'error');
    }
}

function showNotification(message, type = 'info') {
    // Implementación de notificaciones (igual que en movimientos.js)
    const notification = document.createElement('div');
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 600;
        z-index: 1000;
        background-color: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
    `;
    
    document.body.appendChild(notification);
    setTimeout(() => notification.remove(), 3000);
}

// Cerrar modal al hacer clic fuera
window.onclick = function(event) {
    const modal = document.getElementById('bodegaModal');
    if (event.target === modal) {
        closeModal();
    }
};

// Debounce para búsqueda de bodegas

function updateBodegasFilterCount() {
    let count = 0;
    if (document.getElementById('searchBodega').value) count++;
    if (document.getElementById('filterActivo').value) count++;
    
    const badge = document.getElementById('bodegasFilterCount');
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline-flex';
    } else {
        badge.style.display = 'none';
    }
}

function clearBodegasFilters() {
    document.getElementById('searchBodega').value = '';
    document.getElementById('filterActivo').value = '';
    updateBodegasFilterCount();
    renderBodegas(BODEGAS_STATE.bodegas);
}