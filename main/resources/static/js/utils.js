// utils.js - Utilidades generales

// ========== MANEJO DE FECHAS ==========
const DateUtils = {
    formatDate(date) {
        return new Date(date).toLocaleDateString('es-ES');
    },
    
    formatDateTime(date) {
        return new Date(date).toLocaleString('es-ES');
    },
    
    formatTime(date) {
        return new Date(date).toLocaleTimeString('es-ES', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    },
    
    isToday(date) {
        const today = new Date();
        const checkDate = new Date(date);
        return today.toDateString() === checkDate.toDateString();
    },
    
    isThisWeek(date) {
        const today = new Date();
        const checkDate = new Date(date);
        const diffTime = Math.abs(today - checkDate);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays <= 7;
    },
    
    getRelativeTime(date) {
        const now = new Date();
        const diffMs = now - new Date(date);
        const diffSecs = Math.floor(diffMs / 1000);
        const diffMins = Math.floor(diffSecs / 60);
        const diffHours = Math.floor(diffMins / 60);
        const diffDays = Math.floor(diffHours / 24);
        
        if (diffSecs < 60) return 'Hace un momento';
        if (diffMins < 60) return `Hace ${diffMins} minuto${diffMins > 1 ? 's' : ''}`;
        if (diffHours < 24) return `Hace ${diffHours} hora${diffHours > 1 ? 's' : ''}`;
        if (diffDays < 7) return `Hace ${diffDays} día${diffDays > 1 ? 's' : ''}`;
        
        return this.formatDate(date);
    }
};

// ========== MANEJO DE NÚMEROS ==========
const NumberUtils = {
    formatCurrency(amount, currency = 'USD') {
        return new Intl.NumberFormat('es-ES', {
            style: 'currency',
            currency: currency
        }).format(amount);
    },
    
    formatNumber(number) {
        return new Intl.NumberFormat('es-ES').format(number);
    },
    
    formatPercent(number) {
        return new Intl.NumberFormat('es-ES', {
            style: 'percent',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(number / 100);
    },
    
    roundToDecimal(number, decimals = 2) {
        return Math.round(number * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }
};

// ========== VALIDACIONES ==========
const Validators = {
    isEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    },
    
    isStrongPassword(password) {
        // Mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un caracter especial
        const strongRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
        return strongRegex.test(password);
    },
    
    isPhone(phone) {
        const phoneRegex = /^\+?[\d\s\-\(\)]{10,}$/;
        return phoneRegex.test(phone);
    },
    
    isEmpty(value) {
        return value === null || value === undefined || value.toString().trim() === '';
    }
};

// ========== MANIPULACIÓN DEL DOM ==========
const DOMUtils = {
    showElement(selector) {
        const element = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (element) element.style.display = 'block';
    },
    
    hideElement(selector) {
        const element = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (element) element.style.display = 'none';
    },
    
    toggleElement(selector) {
        const element = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (element) {
            element.style.display = element.style.display === 'none' ? 'block' : 'none';
        }
    },
    
    addClass(selector, className) {
        const element = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (element) element.classList.add(className);
    },
    
    removeClass(selector, className) {
        const element = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (element) element.classList.remove(className);
    },
    
    toggleClass(selector, className) {
        const element = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (element) element.classList.toggle(className);
    }
};

// ========== MANEJO DE NOTIFICACIONES ==========
const NotificationManager = {
    container: null,
    
    init() {
        this.container = document.createElement('div');
        this.container.className = 'notification-container';
        document.body.appendChild(this.container);
    },
    
    show(message, type = 'info', title = null, duration = 5000) {
        if (!this.container) this.init();
        
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        
        const notificationId = 'notification-' + Date.now();
        notification.id = notificationId;
        
        notification.innerHTML = `
            <div class="notification-header">
                <span class="notification-title">${title || this.getTitleByType(type)}</span>
                <button class="notification-close" onclick="NotificationManager.close('${notificationId}')">×</button>
            </div>
            <div class="notification-message">${message}</div>
        `;
        
        this.container.appendChild(notification);
        
        // Auto-remover después del tiempo especificado
        setTimeout(() => {
            this.close(notificationId);
        }, duration);
        
        return notificationId;
    },
    
    close(notificationId) {
        const notification = document.getElementById(notificationId);
        if (notification) {
            notification.style.animation = 'slideInRight 0.3s ease reverse';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }
    },
    
    getTitleByType(type) {
        const titles = {
            info: 'Información',
            success: 'Éxito',
            error: 'Error',
            warning: 'Advertencia'
        };
        return titles[type] || 'Notificación';
    },
    
    success(message, title = 'Éxito') {
        return this.show(message, 'success', title);
    },
    
    error(message, title = 'Error') {
        return this.show(message, 'error', title);
    },
    
    warning(message, title = 'Advertencia') {
        return this.show(message, 'warning', title);
    },
    
    info(message, title = 'Información') {
        return this.show(message, 'info', title);
    }
};

// ========== MANEJO DE LOCALSTORAGE MEJORADO ==========
const StorageManager = {
    set(key, value) {
        try {
            const serializedValue = JSON.stringify(value);
            localStorage.setItem(key, serializedValue);
            return true;
        } catch (error) {
            console.error('Error guardando en localStorage:', error);
            return false;
        }
    },
    
    get(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (error) {
            console.error('Error leyendo de localStorage:', error);
            return defaultValue;
        }
    },
    
    remove(key) {
        try {
            localStorage.removeItem(key);
            return true;
        } catch (error) {
            console.error('Error removiendo de localStorage:', error);
            return false;
        }
    },
    
    clear() {
        try {
            localStorage.clear();
            return true;
        } catch (error) {
            console.error('Error limpiando localStorage:', error);
            return false;
        }
    },
    
    exists(key) {
        return localStorage.getItem(key) !== null;
    }
};

// ========== MANEJO DE API ==========
const ApiUtils = {
    async handleResponse(response) {
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ error: 'Error desconocido' }));
            throw new Error(errorData.error || `Error ${response.status}: ${response.statusText}`);
        }
        return response.json();
    },
    
    async get(url, options = {}) {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Bearer': `${Storage.getToken()}`,
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });
        return this.handleResponse(response);
    },
    
    async post(url, data, options = {}) {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Bearer': `${Storage.getToken()}`,
                'Content-Type': 'application/json',
                ...options.headers
            },
            body: JSON.stringify(data),
            ...options
        });
        return this.handleResponse(response);
    },
    
    async put(url, data, options = {}) {
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Bearer': `${Storage.getToken()}`,
                'Content-Type': 'application/json',
                ...options.headers
            },
            body: JSON.stringify(data),
            ...options
        });
        return this.handleResponse(response);
    },
    
    async delete(url, options = {}) {
        const response = await fetch(url, {
            method: 'DELETE',
            headers: {
                'Bearer': `${Storage.getToken()}`,
                ...options.headers
            },
            ...options
        });
        return this.handleResponse(response);
    }
};

// ========== UTILIDADES DE STRING ==========
const StringUtils = {
    capitalize(str) {
        return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    },
    
    truncate(str, length, suffix = '...') {
        return str.length > length ? str.substring(0, length) + suffix : str;
    },
    
    slugify(str) {
        return str
            .toLowerCase()
            .trim()
            .replace(/[^\w\s-]/g, '')
            .replace(/[\s_-]+/g, '-')
            .replace(/^-+|-+$/g, '');
    },
    
    escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
};

// ========== EXPORTAR PARA USO GLOBAL ==========
// Hacer disponibles las utilidades globalmente
window.DateUtils = DateUtils;
window.NumberUtils = NumberUtils;
window.Validators = Validators;
window.DOMUtils = DOMUtils;
window.NotificationManager = NotificationManager;
window.StorageManager = StorageManager;
window.ApiUtils = ApiUtils;
window.StringUtils = StringUtils;

// Inicializar el sistema de notificaciones al cargar
document.addEventListener('DOMContentLoaded', function() {
    NotificationManager.init();
});

// ========== DEBOUNCE PARA BÚSQUEDAS ==========
function debounce(func, wait) {
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

// Exportar para uso global
window.debounce = debounce;