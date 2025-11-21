// Estado global para movimientos
let MOVIMIENTOS_STATE = {
    currentMovementType: null,
    bodegas: [],
    productos: [],
    movimientos: [],
    stock: []
};

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    if (!requireAuth()) return;
    
    loadUserInfo();
    initializeMovimientos();
});

function loadUserInfo() {
    const user = Storage.getUser();
    if (user) {
        document.getElementById('userName').textContent = user.nombre;
    }
}

async function initializeMovimientos() {
    try {
        await loadBodegas();
        await loadProductos();
        
        // Verificar si hay parámetros en URL
        const urlParams = new URLSearchParams(window.location.search);
        const type = urlParams.get('type');
        if (type) {
            selectMovementType(type.toUpperCase());
            showTab('registrar');
        }
        
    } catch (error) {
        console.error('Error inicializando movimientos:', error);
        showNotification('Error al cargar datos iniciales', 'error');
    }
}

// ========== GESTIÓN DE PESTAÑAS ==========
function showTab(tabName) {
    // Ocultar todos los tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Remover active de todos los botones
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Mostrar tab seleccionado
    document.getElementById(tabName).classList.add('active');
    
    // Activar botón correspondiente
    document.querySelector(`.tab-btn[onclick="showTab('${tabName}')"]`).classList.add('active');
    
    // Cargar datos si es necesario
    if (tabName === 'consultar') {
        loadMovimientos();
    } else if (tabName === 'stock') {
        loadStock();
    }
}

// ========== SELECCIÓN DE TIPO DE MOVIMIENTO ==========
function selectMovementType(type) {
    MOVIMIENTOS_STATE.currentMovementType = type;
    
    // Remover selección anterior
    document.querySelectorAll('.type-option').forEach(option => {
        option.classList.remove('selected');
    });
    
    // Agregar selección actual
    document.querySelectorAll('.type-option').forEach(option => {
        if (option.querySelector('h4').textContent.toUpperCase() === type) {
            option.classList.add('selected');
        }
    });
    
    // Mostrar/ocultar campos según tipo
    const bodegaDestinoGroup = document.getElementById('bodegaDestinoGroup');
    if (type === 'TRANSFERENCIA') {
        bodegaDestinoGroup.style.display = 'block';
        document.getElementById('bodegaDestino').required = true;
    } else {
        bodegaDestinoGroup.style.display = 'none';
        document.getElementById('bodegaDestino').required = false;
    }
    
    // Limpiar y agregar primera fila de producto
    document.getElementById('productRows').innerHTML = '';
    addProductRow();
}

// ========== GESTIÓN DE FILAS DE PRODUCTOS ==========
function addProductRow() {
    const productRows = document.getElementById('productRows');
    const rowId = Date.now(); // ID único
    
    const rowHTML = `
        <div class="product-row" id="productRow-${rowId}">
            <div class="form-group">
                <label>Producto</label>
                <select name="producto" required onchange="updateProductPrice(${rowId})">
                    <option value="">Seleccionar producto...</option>
                    ${MOVIMIENTOS_STATE.productos.map(producto => 
                        `<option value="${producto.id}" data-precio="${producto.precio}">
                            ${producto.nombre} - $${producto.precio}
                        </option>`
                    ).join('')}
                </select>
            </div>
            <div class="form-group">
                <label>Cantidad</label>
                <input type="number" name="cantidad" min="1" required value="1">
            </div>
            <div class="form-group">
                <label>Precio Unitario</label>
                <input type="number" name="precioUnitario" step="0.01" min="0" required 
                       placeholder="0.00" id="precio-${rowId}">
            </div>
            <button type="button" class="remove-product" onclick="removeProductRow(${rowId})">
                ×
            </button>
        </div>
    `;
    
    productRows.insertAdjacentHTML('beforeend', rowHTML);
}

function removeProductRow(rowId) {
    const row = document.getElementById(`productRow-${rowId}`);
    if (row) {
        row.remove();
    }
    
    // Si no quedan filas, agregar una nueva
    const remainingRows = document.querySelectorAll('.product-row').length;
    if (remainingRows === 0) {
        addProductRow();
    }
}

function updateProductPrice(rowId) {
    const select = document.querySelector(`#productRow-${rowId} select[name="producto"]`);
    const priceInput = document.getElementById(`precio-${rowId}`);
    
    if (select && select.value && priceInput) {
        const selectedOption = select.options[select.selectedIndex];
        const precio = selectedOption.getAttribute('data-precio');
        priceInput.value = precio || '0.00';
    }
}

// ========== CARGA DE DATOS ==========
async function loadBodegas() {
    try {
        const response = await fetch(buildUrl(API_CONFIG.ENDPOINTS.BODEGAS), {
            headers: {
                'Authorization': `Bearer ${Storage.getToken()}`
            }
        });
        
        if (response.ok) {
            MOVIMIENTOS_STATE.bodegas = await response.json();
            
            // Llenar selects de bodegas
            const bodegaOrigenSelect = document.getElementById('bodegaOrigen');
            const bodegaDestinoSelect = document.getElementById('bodegaDestino');
            const stockBodegaSelect = document.getElementById('stockBodega');
            
            const bodegasHTML = MOVIMIENTOS_STATE.bodegas.map(bodega => 
                `<option value="${bodega.id}">${bodega.nombre}</option>`
            ).join('');
            
            [bodegaOrigenSelect, bodegaDestinoSelect, stockBodegaSelect].forEach(select => {
                if (select) {
                    select.innerHTML = '<option value="">Seleccionar bodega...</option>' + bodegasHTML;
                }
            });
        }
    } catch (error) {
        console.error('Error cargando bodegas:', error);
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
            MOVIMIENTOS_STATE.productos = await response.json();
        }
    } catch (error) {
        console.error('Error cargando productos:', error);
    }
}

// ========== REGISTRO DE MOVIMIENTOS ==========
document.getElementById('movementForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    if (!MOVIMIENTOS_STATE.currentMovementType) {
        showNotification('Selecciona un tipo de movimiento', 'error');
        return;
    }
    
    try {
        const formData = new FormData(this);
        const movimientoData = buildMovimientoData(formData);
        
        // 🔧 Validar que haya productos
        if (movimientoData.detalles.length === 0) {
            throw new Error('Debes agregar al menos un producto');
        }
        
        // 🔧 Obtener y validar bodegas
        const bodegaOrigenId = document.getElementById('bodegaOrigen').value;
        const bodegaDestinoId = document.getElementById('bodegaDestino').value;
        
        if (!bodegaOrigenId) {
            throw new Error('Debes seleccionar una bodega de origen');
        }
        
        // 🔧 Construir endpoint según tipo
        let endpoint;
        switch (MOVIMIENTOS_STATE.currentMovementType) {
            case 'ENTRADA':
                endpoint = `${API_CONFIG.BASE_URL}/movimientos/entrada/${bodegaOrigenId}`;
                break;
            case 'SALIDA':
                endpoint = `${API_CONFIG.BASE_URL}/movimientos/salida/${bodegaOrigenId}`;
                break;
            case 'TRANSFERENCIA':
                if (!bodegaDestinoId) {
                    throw new Error('Debes seleccionar una bodega de destino para transferencias');
                }
                endpoint = `${API_CONFIG.BASE_URL}/movimientos/transferencia/${bodegaOrigenId}/${bodegaDestinoId}`;
                break;
            default:
                throw new Error('Tipo de movimiento no válido');
        }
        
        console.log('📤 Enviando a:', endpoint);
        console.log('📦 Datos:', movimientoData);
        console.log('🔑 Token:', Storage.getToken());
        
        // 🔧 Hacer la petición
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${Storage.getToken()}`
            },
            body: JSON.stringify(movimientoData)
        });
        
        console.log('📥 Response status:', response.status);
        const responseData = await response.text();
        console.log('📥 Response body:', responseData);
        
        if (response.ok) {
            showNotification('Movimiento registrado exitosamente', 'success');
            resetForm();
            if (document.getElementById('consultar').classList.contains('active')) {
                loadMovimientos();
            }
        } else {
            let errorMessage = 'Error al registrar movimiento';
            try {
                const errorData = JSON.parse(responseData);
                errorMessage = errorData.error || errorData.message || errorMessage;
            } catch (e) {
                errorMessage = responseData || errorMessage;
            }
            throw new Error(errorMessage);
        }
        
    } catch (error) {
        console.error('❌ Error registrando movimiento:', error);
        showNotification(error.message, 'error');
    }
});

function buildMovimientoData(formData) {
    const productoRows = document.querySelectorAll('.product-row');
    const detalles = [];
    
    productoRows.forEach(row => {
        const productoSelect = row.querySelector('select[name="producto"]');
        const cantidadInput = row.querySelector('input[name="cantidad"]');
        const precioInput = row.querySelector('input[name="precioUnitario"]');
        
        if (productoSelect.value && cantidadInput.value) {
            detalles.push({
                productoId: parseInt(productoSelect.value),
                cantidad: parseInt(cantidadInput.value),
                precioUnitario: parseFloat(precioInput.value) || 0
            });
        }
    });
    
    return {
        detalles: detalles,
        observaciones: formData.get('observaciones') || ''
    };
}

function getMovimientoEndpoint() {
    const bodegaOrigenId = document.getElementById('bodegaOrigen').value;
    const bodegaDestinoId = document.getElementById('bodegaDestino').value;
    
    switch (MOVIMIENTOS_STATE.currentMovementType) {
        case 'ENTRADA':
            return buildUrl(`${API_CONFIG.ENDPOINTS.MOVIMIENTOS}/entrada/{bodegaId}`, {
                bodegaId: bodegaOrigenId
            });
        case 'SALIDA':
            return buildUrl(`${API_CONFIG.ENDPOINTS.MOVIMIENTOS}/salida/{bodegaId}`, {
                bodegaId: bodegaOrigenId
            });
        case 'TRANSFERENCIA':
            return buildUrl(`${API_CONFIG.ENDPOINTS.MOVIMIENTOS}/transferencia/{origenId}/{destinoId}`, {
                origenId: bodegaOrigenId,
                destinoId: bodegaDestinoId
            });
        default:
            throw new Error('Tipo de movimiento no válido');
    }
}

function resetForm() {
    document.getElementById('movementForm').reset();
    document.getElementById('productRows').innerHTML = '';
    MOVIMIENTOS_STATE.currentMovementType = null;
    
    document.querySelectorAll('.type-option').forEach(option => {
        option.classList.remove('selected');
    });
    
    document.getElementById('bodegaDestinoGroup').style.display = 'none';
    addProductRow();
}

// ========== CONSULTA DE MOVIMIENTOS ==========
async function loadMovimientos() {
    try {
        const container = document.getElementById('movimientosList');
        container.innerHTML = '<div class="loading">Cargando movimientos...</div>';
        
        // Construir URL con filtros
        let url = buildUrl(API_CONFIG.ENDPOINTS.MOVIMIENTOS);
        const params = new URLSearchParams();
        
        const tipo = document.getElementById('filterTipo').value;
        const fechaInicio = document.getElementById('filterFechaInicio').value;
        const fechaFin = document.getElementById('filterFechaFin').value;
        
        if (tipo) params.append('tipo', tipo);
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
            MOVIMIENTOS_STATE.movimientos = await response.json();
            renderMovimientos();
        } else {
            throw new Error('Error al cargar movimientos');
        }
        
    } catch (error) {
        console.error('Error cargando movimientos:', error);
        document.getElementById('movimientosList').innerHTML = 
            '<div class="loading">Error al cargar movimientos</div>';
    }
}

function renderMovimientos() {
    const container = document.getElementById('movimientosList');
    
    if (MOVIMIENTOS_STATE.movimientos.length === 0) {
        container.innerHTML = '<div class="loading">No se encontraron movimientos</div>';
        return;
    }
    
    container.innerHTML = MOVIMIENTOS_STATE.movimientos.map(movimiento => `
        <div class="movement-item">
            <div class="movement-icon">
                ${getMovementIcon(movimiento.tipo)}
            </div>
            <div class="movement-info">
                <h4>${movimiento.tipo} - ${movimiento.bodegaOrigen?.nombre || 'N/A'}</h4>
                <p><strong>Usuario:</strong> ${movimiento.usuario?.nombre || 'N/A'}</p>
                <p><strong>Fecha:</strong> ${new Date(movimiento.fecha).toLocaleString()}</p>
                <p><strong>Productos:</strong> ${movimiento.detalles?.length || 0} items</p>
                ${movimiento.observaciones ? `<p><strong>Observaciones:</strong> ${movimiento.observaciones}</p>` : ''}
            </div>
            <span class="movement-badge badge-${movimiento.tipo.toLowerCase()}">
                ${movimiento.tipo}
            </span>
        </div>
    `).join('');
}

function getMovementIcon(tipo) {
    switch (tipo) {
        case 'ENTRADA': return '⬇️';
        case 'SALIDA': return '⬆️';
        case 'TRANSFERENCIA': return '🔄';
        default: return '📦';
    }
}

// ========== CONSULTA DE STOCK ==========
let STOCK_DATA = [];
async function loadStock() {
    try {
        const bodegaId = document.getElementById('stockBodega').value;
        const container = document.getElementById('stockList');
        const statsContainer = document.getElementById('stockStats');
        
        // ✅ Verificar que los elementos existen
        if (!container) {
            console.error('No se encontró el elemento stockList');
            return;
        }
        
        if (!bodegaId) {
            container.innerHTML = '<div class="loading">⚠️ Selecciona una bodega para ver el stock</div>';
            // ✅ Solo ocultar si existe
            if (statsContainer) {
                statsContainer.style.display = 'none';
            }
            // Deshabilitar filtros secundarios
            const searchInput = document.getElementById('searchStockProducto');
            const filterSelect = document.getElementById('filterStockBajo');
            if (searchInput) searchInput.disabled = true;
            if (filterSelect) filterSelect.disabled = true;
            return;
        }
        
        container.innerHTML = '<div class="loading">Cargando stock...</div>';
        // ✅ Solo ocultar si existe
        if (statsContainer) {
            statsContainer.style.display = 'none';
        }
        
        // Hacer petición al backend
        const response = await fetch(buildUrl(`${API_CONFIG.ENDPOINTS.INVENTARIOS}/bodega/${bodegaId}`), {
            headers: {
                'Authorization': `Bearer ${Storage.getToken()}`
            }
        });
        
        if (response.ok) {
            const inventarios = await response.json();
            
            // Si no hay inventarios, intentar con todos y filtrar
            if (!inventarios || inventarios.length === 0) {
                const allResponse = await fetch(buildUrl(API_CONFIG.ENDPOINTS.INVENTARIOS), {
                    headers: {
                        'Authorization': `Bearer ${Storage.getToken()}`
                    }
                });
                
                if (allResponse.ok) {
                    const allInventarios = await allResponse.json();
                    STOCK_DATA = allInventarios.filter(inv => inv.bodegaId == bodegaId);
                } else {
                    STOCK_DATA = [];
                }
            } else {
                STOCK_DATA = inventarios;
            }
            
            // Cargar información de productos si es necesario
            await enrichStockWithProductInfo();
            
            // Habilitar filtros secundarios
            const searchInput = document.getElementById('searchStockProducto');
            const filterSelect = document.getElementById('filterStockBajo');
            if (searchInput) searchInput.disabled = false;
            if (filterSelect) filterSelect.disabled = false;
            
            // Renderizar stock
            renderStock(STOCK_DATA);
            
            // Mostrar estadísticas
            updateStockStats(STOCK_DATA);
            // ✅ Solo mostrar si existe
            if (statsContainer) {
                statsContainer.style.display = 'grid';
            }
            
        } else {
            throw new Error('Error al cargar stock');
        }
        
    } catch (error) {
        console.error('Error cargando stock:', error);
        const container = document.getElementById('stockList');
        if (container) {
            container.innerHTML = '<div class="loading">❌ Error al cargar el stock. Intenta nuevamente.</div>';
        }
    }
}

// Enriquecer datos de stock con información de productos
async function enrichStockWithProductInfo() {
    // Si ya tenemos productos cargados, úsalos
    if (MOVIMIENTOS_STATE.productos && MOVIMIENTOS_STATE.productos.length > 0) {
        STOCK_DATA = STOCK_DATA.map(inv => {
            const producto = MOVIMIENTOS_STATE.productos.find(p => p.id == inv.productoId);
            return {
                ...inv,
                producto: producto || { id: inv.productoId, nombre: `Producto ${inv.productoId}`, precio: 0, categoria: 'N/A' }
            };
        });
    }
}

// Renderizar lista de stock
function renderStock(stockData) {
    const container = document.getElementById('stockList');
    
    if (!stockData || stockData.length === 0) {
        container.innerHTML = '<div class="loading">📦 No hay productos en esta bodega</div>';
        return;
    }
    
    container.innerHTML = stockData.map(item => {
        const producto = item.producto || { nombre: `Producto ${item.productoId}`, precio: 0, categoria: 'N/A' };
        const stockLevel = getStockLevel(item.stockActual);
        
        return `
            <div class="stock-item">
                <div class="stock-icon">📦</div>
                <div class="stock-info">
                    <h4>${producto.nombre}</h4>
                    <p><strong>Categoría:</strong> ${producto.categoria}</p>
                    <p><strong>Precio referencia:</strong> $${Number(producto.precio).toFixed(2)}</p>
                    <p><strong>Valor en stock:</strong> $${(item.stockActual * Number(producto.precio)).toFixed(2)}</p>
                </div>
                <div style="text-align: right;">
                    <span class="stock-badge" style="font-size: 18px; display: block; margin-bottom: 8px;">
                        ${item.stockActual} unidades
                    </span>
                    <span class="stock-level-badge stock-level-${stockLevel}">
                        ${stockLevel === 'bajo' ? '⚠️ Bajo' : stockLevel === 'medio' ? '📊 Medio' : '✅ Alto'}
                    </span>
                </div>
            </div>
        `;
    }).join('');
}

// Determinar nivel de stock
function getStockLevel(cantidad) {
    if (cantidad < 10) return 'bajo';
    if (cantidad <= 50) return 'medio';
    return 'alto';
}

// Actualizar estadísticas de stock
function updateStockStats(stockData) {
    if (!stockData || stockData.length === 0) {
        return;
    }
    
    const totalProductos = stockData.length;
    const totalUnidades = stockData.reduce((sum, item) => sum + item.stockActual, 0);
    const stockBajo = stockData.filter(item => item.stockActual < 10).length;
    
    // Calcular valor total
    let valorTotal = 0;
    stockData.forEach(item => {
        const precio = item.producto?.precio || 0;
        valorTotal += item.stockActual * Number(precio);
    });
    
    document.getElementById('totalProductosStock').textContent = totalProductos;
    document.getElementById('totalUnidadesStock').textContent = totalUnidades;
    document.getElementById('stockBajoCount').textContent = stockBajo;
    document.getElementById('valorTotalStock').textContent = `$${valorTotal.toFixed(2)}`;
}

// Filtrar lista de stock
function filterStockList() {
    const searchText = document.getElementById('searchStockProducto').value.toLowerCase();
    const nivelStock = document.getElementById('filterStockBajo').value;
    
    let filtered = [...STOCK_DATA];
    
    // Filtrar por búsqueda de texto
    if (searchText) {
        filtered = filtered.filter(item => {
            const producto = item.producto || {};
            return (producto.nombre || '').toLowerCase().includes(searchText) ||
                   (producto.categoria || '').toLowerCase().includes(searchText);
        });
    }
    
    // Filtrar por nivel de stock
    if (nivelStock) {
        filtered = filtered.filter(item => {
            const nivel = getStockLevel(item.stockActual);
            return nivel === nivelStock;
        });
    }
    
    renderStock(filtered);
    updateStockStats(filtered);
}

// Actualizar contador de filtros de stock
function updateStockFilterCount() {
    let count = 0;
    if (document.getElementById('stockBodega').value) count++;
    
    const badge = document.getElementById('stockFilterCount');
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline-flex';
    } else {
        badge.style.display = 'none';
    }
}

// Limpiar filtros de stock
function clearStockFilters() {
    // ✅ Validar que cada elemento existe antes de usarlo
    const bodegaSelect = document.getElementById('stockBodega');
    const searchInput = document.getElementById('searchStockProducto');
    const filterSelect = document.getElementById('filterStockBajo');
    const stockList = document.getElementById('stockList');
    const statsContainer = document.getElementById('stockStats');
    
    if (bodegaSelect) bodegaSelect.value = '';
    if (searchInput) {
        searchInput.value = '';
        searchInput.disabled = true;
    }
    if (filterSelect) {
        filterSelect.value = '';
        filterSelect.disabled = true;
    }
    
    updateStockFilterCount();
    
    if (stockList) {
        stockList.innerHTML = '<div class="loading">Selecciona una bodega y haz clic en "Consultar Stock"</div>';
    }
    
    if (statsContainer) {
        statsContainer.style.display = 'none';
    }
}

// ========== UTILIDADES ==========
function showNotification(message, type = 'info') {
    // Crear notificación temporal
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
        animation: slideIn 0.3s ease;
    `;
    
    if (type === 'success') {
        notification.style.backgroundColor = '#10b981';
    } else if (type === 'error') {
        notification.style.backgroundColor = '#ef4444';
    } else {
        notification.style.backgroundColor = '#3b82f6';
    }
    
    document.body.appendChild(notification);
    
    // Remover después de 3 segundos
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// Navegación
function navigateTo(url) {
    window.location.href = url;
}

// Contar filtros activos en movimientos
function updateFilterCount() {
    let count = 0;
    if (document.getElementById('filterTipo').value) count++;
    if (document.getElementById('filterFechaInicio').value) count++;
    if (document.getElementById('filterFechaFin').value) count++;
    
    const badge = document.getElementById('activeFiltersCount');
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline-flex';
    } else {
        badge.style.display = 'none';
    }
}

function clearMovimientosFilters() {
    document.getElementById('filterTipo').value = '';
    document.getElementById('filterFechaInicio').value = '';
    document.getElementById('filterFechaFin').value = '';
    updateFilterCount();
    loadMovimientos();
}