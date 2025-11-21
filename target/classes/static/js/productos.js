let PRODUCTOS_STATE = {
    productos: [],
    editingProducto: null
};

const debouncedFilterProductos = debounce(filterProductos, 300);

document.addEventListener('DOMContentLoaded', function() {
    if (!requireAuth()) return;
    
    loadUserInfo();
    loadProductos();
});

function loadUserInfo() {
    const user = Storage.getUser();
    if (user) {
        document.getElementById('userName').textContent = user.nombre;
    }
}

async function loadProductos() {
    try {
        const response = await fetch(buildUrl(API_CONFIG.ENDPOINTS.PRODUCTOS), {
            headers: {
                'Authorization': `Bearer ${Storage.getToken()}`
            }
        });
        
        if (response.ok) {
            PRODUCTOS_STATE.productos = await response.json();
            renderProductos();
        } else {
            throw new Error('Error al cargar productos');
        }
    } catch (error) {
        console.error('Error:', error);
        document.getElementById('productosList').innerHTML = 
            '<div class="loading">Error al cargar productos</div>';
    }
}

function renderProductos(productos = PRODUCTOS_STATE.productos) {
    const container = document.getElementById('productosList');
    
    if (productos.length === 0) {
        container.innerHTML = '<div class="loading">No se encontraron productos</div>';
        return;
    }
    
    container.innerHTML = `
        <table class="productos-table">
            <thead>
                <tr>
                    <th>Producto</th>
                    <th>Categoría</th>
                    <th>Precio</th>
                    <th>Estado</th>
                    <th>Fecha Creación</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                ${productos.map(producto => `
                    <tr>
                        <td>
                            <div class="producto-name">${producto.nombre}</div>
                        </td>
                        <td>
                            <span class="producto-category">${producto.categoria}</span>
                        </td>
                        <td class="producto-price">$${producto.precio}</td>
                        <td>
                            <span class="producto-status ${producto.activo ? 'status-active' : 'status-inactive'}">
                                ${producto.activo ? 'Activo' : 'Inactivo'}
                            </span>
                        </td>
                        <td>${new Date(producto.createdAt).toLocaleDateString()}</td>
                        <td>
                            <div class="producto-actions">
                                <button class="btn btn-primary btn-sm" onclick="editProducto(${producto.id})">Editar</button>
                                <button class="btn btn-secondary btn-sm" onclick="toggleProductoStatus(${producto.id}, ${!producto.activo})">
                                    ${producto.activo ? 'Desactivar' : 'Activar'}
                                </button>
                            </div>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

function filterProductos() {
    const search = document.getElementById('searchProducto').value.toLowerCase();
    const categoria = document.getElementById('filterCategoria').value;
    const estado = document.getElementById('filterActivo').value;
    
    let filtered = PRODUCTOS_STATE.productos;
    
    if (search) {
        filtered = filtered.filter(producto => 
            producto.nombre.toLowerCase().includes(search) ||
            producto.categoria.toLowerCase().includes(search)
        );
    }
    
    if (categoria) {
        filtered = filtered.filter(producto => producto.categoria === categoria);
    }
    
    if (estado !== '') {
        filtered = filtered.filter(producto => 
            producto.activo === (estado === 'true')
        );
    }
    
    renderProductos(filtered);
}

function showProductModal(mode, producto = null) {
    PRODUCTOS_STATE.editingProducto = producto;
    const modal = document.getElementById('productoModal');
    const title = document.getElementById('productoModalTitle');
    
    if (mode === 'create') {
        title.textContent = 'Nuevo Producto';
        document.getElementById('productoForm').reset();
        document.getElementById('productoActivo').checked = true;
    } else {
        title.textContent = 'Editar Producto';
        fillProductFormWithData(producto);
    }
    
    modal.style.display = 'block';
}

function closeProductModal() {
    document.getElementById('productoModal').style.display = 'none';
    PRODUCTOS_STATE.editingProducto = null;
}

function fillProductFormWithData(producto) {
    document.getElementById('productoNombre').value = producto.nombre || '';
    document.getElementById('productoCategoria').value = producto.categoria || '';
    document.getElementById('productoPrecio').value = producto.precio || '';
    document.getElementById('productoActivo').checked = producto.activo !== false;
}

document.getElementById('productoForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    const productoData = {
        nombre: formData.get('nombre'),
        categoria: formData.get('categoria'),
        precio: parseFloat(formData.get('precio')),
        activo: formData.get('activo') === 'on'
    };
    
    try {
        let response;
        
        if (PRODUCTOS_STATE.editingProducto) {
            response = await fetch(buildUrl(`${API_CONFIG.ENDPOINTS.PRODUCTOS}/{id}`, {
                id: PRODUCTOS_STATE.editingProducto.id
            }), {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${Storage.getToken()}`
                },
                body: JSON.stringify(productoData)
            });
        } else {
            response = await fetch(buildUrl(API_CONFIG.ENDPOINTS.PRODUCTOS), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${Storage.getToken()}`
                },
                body: JSON.stringify(productoData)
            });
        }
        
        if (response.ok) {
            showNotification(
                PRODUCTOS_STATE.editingProducto ? 'Producto actualizado' : 'Producto creado', 
                'success'
            );
            closeProductModal();
            loadProductos();
        } else {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Error al guardar producto');
        }
        
    } catch (error) {
        console.error('Error:', error);
        showNotification(error.message, 'error');
    }
});

async function editProducto(id) {
    const producto = PRODUCTOS_STATE.productos.find(p => p.id === id);
    if (producto) {
        showProductModal('edit', producto);
    }
}

async function toggleProductoStatus(id, newStatus) {
    try {
        const producto = PRODUCTOS_STATE.productos.find(p => p.id === id);
        if (!producto) return;
        
        const response = await fetch(buildUrl(`${API_CONFIG.ENDPOINTS.PRODUCTOS}/{id}`, { id }), {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${Storage.getToken()}`
            },
            body: JSON.stringify({
                ...producto,
                activo: newStatus
            })
        });
        
        if (response.ok) {
            showNotification(
                `Producto ${newStatus ? 'activado' : 'desactivado'}`,
                'success'
            );
            loadProductos();
        } else {
            throw new Error('Error al cambiar estado');
        }
        
    } catch (error) {
        console.error('Error:', error);
        showNotification('Error al cambiar estado', 'error');
    }
}

function showNotification(message, type = 'info') {
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

window.onclick = function(event) {
    const modal = document.getElementById('productoModal');
    if (event.target === modal) {
        closeProductModal();
    }
};

// Debounce para búsqueda de productos

function updateProductosFilterCount() {
    let count = 0;
    if (document.getElementById('searchProducto').value) count++;
    if (document.getElementById('filterCategoria').value) count++;
    if (document.getElementById('filterActivo').value) count++;
    
    const badge = document.getElementById('productosFilterCount');
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline-flex';
    } else {
        badge.style.display = 'none';
    }
}

function clearProductosFilters() {
    document.getElementById('searchProducto').value = '';
    document.getElementById('filterCategoria').value = '';
    document.getElementById('filterActivo').value = '';
    updateProductosFilterCount();
    renderProductos(PRODUCTOS_STATE.productos);
}