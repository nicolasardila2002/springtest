// Configuración global de la aplicación
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080/logitrack/api',
    ENDPOINTS: {
        AUTH: {
            LOGIN: '/auth/login',
            REGISTER: '/auth/register',
            ME: '/auth/me',
            VERIFY: '/auth/verify',
            LOGOUT: '/auth/logout'
        },
        MOVIMIENTOS: {
            BASE: '/movimientos',
            ENTRADA: '/movimientos/entrada',
            SALIDA: '/movimientos/salida',
            TRANSFERENCIA: '/movimientos/transferencia'
        },
        BODEGAS: '/bodegas',
        PRODUCTOS: '/productos',
        INVENTARIOS: '/inventarios',
        AUDITORIA: '/auditoria'
    },
    ROLES: {
        ADMIN: 'ADMIN',
        EMPLEADO: 'EMPLEADO'
    },
    TIPOS_MOVIMIENTO: {
        ENTRADA: 'ENTRADA',
        SALIDA: 'SALIDA',
        TRANSFERENCIA: 'TRANSFERENCIA'
    }
};

// Estado global de la aplicación
let APP_STATE = {
    token: localStorage.getItem('token'),
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    isAuthenticated: !!localStorage.getItem('token')
};

// Utilidades para localStorage
const Storage = {
    setToken(token) {
        localStorage.setItem('token', token);
        APP_STATE.token = token;
        APP_STATE.isAuthenticated = true;
    },
    
    setUser(user) {
        localStorage.setItem('user', JSON.stringify(user));
        APP_STATE.user = user;
    },
    
    clear() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        APP_STATE.token = null;
        APP_STATE.user = null;
        APP_STATE.isAuthenticated = false;
    },
    
    getToken() {
        return APP_STATE.token;
    },
    
    getUser() {
        return APP_STATE.user;
    },
    
    isAuthenticated() {
        return APP_STATE.isAuthenticated;
    }
};

// Función para construir URLs de API
function buildUrl(endpoint, params = {}) {
    // Permitir pasar un objeto con propiedades (p.ej. { BASE: '/movimientos', ... })
    if (typeof endpoint === 'object' && endpoint !== null) {
        if (endpoint.BASE) {
            endpoint = endpoint.BASE;
        } else {
            // Si no tiene BASE, intentar convertir a string seguro
            endpoint = String(endpoint);
        }
    }

    let url = API_CONFIG.BASE_URL + endpoint;

    // Reemplazar parámetros en la URL (ej: /api/movimientos/entrada/{bodegaId})
    Object.keys(params).forEach(key => {
        url = url.replace(`{${key}}`, params[key]);
    });

    return url;
}

function debounce(func, wait = 300) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}