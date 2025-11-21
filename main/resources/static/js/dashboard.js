// Dashboard functionality
document.addEventListener('DOMContentLoaded', function() {
    if (!requireAuth()) return;
    
    loadUserInfo();
    loadDashboardData();
    loadRecentMovimientos();
});

function loadUserInfo() {
    const user = Storage.getUser();
    if (user) {
        document.getElementById('userName').textContent = user.nombre;
    }
}

async function loadDashboardData() {
    try {
        // Cargar productos
        const productosResponse = await fetch(buildUrl(API_CONFIG.ENDPOINTS.PRODUCTOS), {
            headers: {
                'Authorization': `Bearer ${Storage.getToken()}`
            }
        });
        
        if (productosResponse.ok) {
            const productos = await productosResponse.json();
            document.getElementById('totalProductos').textContent = productos.length;
        }

        // Cargar bodegas
        const bodegasResponse = await fetch(buildUrl(API_CONFIG.ENDPOINTS.BODEGAS), {
            headers: {
                'Authorization': `Bearer ${Storage.getToken()}`
            }
        });
        
        if (bodegasResponse.ok) {
            const bodegas = await bodegasResponse.json();
            document.getElementById('totalBodegas').textContent = bodegas.length;
        }

        // Cargar movimientos de hoy (simulado por ahora)
        const movimientosResponse = await fetch(buildUrl(API_CONFIG.ENDPOINTS.MOVIMIENTOS), {
            headers: {
                'Authorization': `Bearer ${Storage.getToken()}`
            }
        });
        
        if (movimientosResponse.ok) {
            const movimientos = await movimientosResponse.json();
            document.getElementById('movimientosHoy').textContent = movimientos.length;
        }
        
        // Cargar usuarios
        document.getElementById('usuariosActivos').textContent = ("2");

    } catch (error) {
        console.error('Error cargando datos del dashboard:', error);
    }
}

async function loadRecentMovimientos() {
    try {
        const response = await fetch(buildUrl(API_CONFIG.ENDPOINTS.MOVIMIENTOS), {
            headers: {
                'Authorization': `Bearer ${Storage.getToken()}`
            }
        });

        const container = document.getElementById('recentMovimientos');

        // Leer siempre el body como texto (permite inspeccionarlo y evitar leer el stream dos veces)
        const bodyText = await response.text();

        if (!response.ok) {
            console.error('Respuesta no OK de /api/movimientos:', bodyText);
            container.innerHTML = '<div class="loading">No hay movimientos recientes</div>';
            return;
        }

        let movimientos;
        try {
            movimientos = JSON.parse(bodyText || '[]');
        } catch (parseError) {
            console.error('Respuesta no es JSON válido (body):', bodyText);
            console.error('Error al parsear JSON:', parseError);
            container.innerHTML = '<div class="loading">Error al procesar datos de movimientos</div>';
            return;
        }
        
        if (movimientos.length === 0) {
            container.innerHTML = '<div class="loading">No hay movimientos recientes</div>';
            return;
        }
        
        // Mostrar últimos 5 movimientos
        const recentMovimientos = movimientos.slice(0, 5);
        
        container.innerHTML = recentMovimientos.map(movimiento => `
            <div class="recent-item">
                <div class="recent-info">
                    <h4>${movimiento.tipo} - Bodega ${movimiento.bodegaOrigen?.nombre || 'N/A'}</h4>
                    <p>${new Date(movimiento.fecha).toLocaleString()}</p>
                </div>
                <span class="recent-badge badge-${movimiento.tipo.toLowerCase()}">
                    ${movimiento.tipo}
                </span>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error cargando movimientos recientes:', error);
        document.getElementById('recentMovimientos').innerHTML = 
            '<div class="loading">Error al cargar movimientos</div>';
    }
}

function navigateTo(url) {
    window.location.href = url;
}

// Actualizar cada 30 segundos
setInterval(loadDashboardData, 30000);