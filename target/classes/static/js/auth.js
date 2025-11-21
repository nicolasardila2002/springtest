// Manejo del formulario de login
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    // Si ya está autenticado, redirigir al dashboard
    if (Storage.isAuthenticated() && window.location.pathname.includes('index.html')) {
        window.location.href = 'dashboard.html';
    }
});

async function handleLogin(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const loginData = {
        email: formData.get('email'),
        password: formData.get('password')
    };
    
    try {
        const response = await fetch(buildUrl(API_CONFIG.ENDPOINTS.AUTH.LOGIN), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Error en el login');
        }
        
        const data = await response.json();
        console.log(data)
        
        // Guardar token y información del usuario
        Storage.setToken(data.token);
        Storage.setUser({
            id: data.id,
            nombre: data.nombre,
            email: data.email,
            rol: data.rol
        });
        
        // Redirigir al dashboard
        window.location.href = 'dashboard.html';
        
    } catch (error) {
        showError(error.message);
    }
}

function showError(message) {
    // Crear o mostrar elemento de error
    let errorElement = document.querySelector('.error-message');
    
    if (!errorElement) {
        errorElement = document.createElement('div');
        errorElement.className = 'error-message';
        document.querySelector('.auth-form').prepend(errorElement);
    }
    
    errorElement.textContent = message;
    errorElement.style.cssText = `
        background-color: #fef2f2;
        border: 1px solid var(--error-color);
        color: var(--error-color);
        padding: 12px 16px;
        border-radius: 8px;
        margin-bottom: 20px;
        font-size: 14px;
    `;
    
    // Auto-remover después de 5 segundos
    setTimeout(() => {
        errorElement.remove();
    }, 5000);
}

// Función de logout
function logout() {
    Storage.clear();
    window.location.href = 'index.html';
}

// Verificar autenticación en páginas protegidas
function requireAuth() {
    if (!Storage.isAuthenticated()) {
        window.location.href = 'index.html';
        return false;
    }
    return true;
}