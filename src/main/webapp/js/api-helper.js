// ===== GESTION CENTRALISÉE DES RÉPONSES API =====
function handleApiResponse(response, onSuccess, onError) {
    // Déterminer le statut
    const isSuccess = response.status === 'success' || (response.code >= 200 && response.code < 300);
    const isError = response.status === 'error' || response.code >= 400;
    
    console.log('API Response:', response);
    
    if (isError) {
        // Extraire le message d'erreur
        const errorMessage = response.error?.message || response.message || 'Erreur inconnue';
        const errorCode = response.code || response.error?.code || 500;
        
        console.error('API Error:', errorCode, errorMessage);
        
        if (onError) {
            onError(errorCode, errorMessage);
        } else {
            showApiError(errorCode, errorMessage);
        }
        return false;
    }
    
    if (isSuccess) {
        if (onSuccess) {
            onSuccess(response.data || response);
        }
        return true;
    }
    
    return false;
}

// ===== AFFICHAGE CENTRALISÉ DES ERREURS =====
function showApiError(code, message) {
    const errorDiv = document.getElementById('loadingDiv');
    if (errorDiv) {
        let icon = '❌';
        let title = 'Erreur';
        
        if (code === 403) {
            icon = '🔐';
            title = 'Accès refusé';
        } else if (code === 404) {
            icon = '🔍';
            title = 'Non trouvé';
        } else if (code === 500) {
            icon = '⚠️';
            title = 'Erreur serveur';
        }
        
        errorDiv.innerHTML = '' +
            '<div class="alert alert-danger alert-dismissible fade show" role="alert">' +
                '<strong>' + icon + ' ' + title + ' (' + code + ')</strong><br>' +
                message +
                '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
            '</div>';
    }
}

// ===== GESTION DU TOKEN =====
function getStoredToken() {
    return localStorage.getItem('api_token');
}

function setStoredToken(token) {
    localStorage.setItem('api_token', token);
}

function clearStoredToken() {
    localStorage.removeItem('api_token');
}

function promptForToken() {
    const token = prompt('Veuillez entrer le token d\'authentification:');
    if (token && token.trim()) {
        setStoredToken(token.trim());
        return token.trim();
    }
    return null;
}

// ===== FETCH AVEC GESTION DU TOKEN =====
async function fetchApi(url, options = {},requireToken = true) {
    let token = getStoredToken();
    console.log('Token actuel:', token);
    
    if (requireToken && !token) {
        token = promptForToken();
        if (!token) {
            throw new Error('Token requis');
        }
    }
    
    // Déterminer la méthode HTTP
    const method = options.method || 'GET';
    
    // Ajouter le token dans l'URL pour GET/DELETE, dans le body pour POST/PUT
    let finalUrl = url;
    let finalOptions = {
        method: method,
        ...options
    };
    
    if (method === 'GET' || method === 'DELETE') {
        // Pour GET et DELETE, ajouter le token dans l'URL
        const separator = url.includes('?') ? '&' : '?';
        finalUrl = url + separator + 'token=' + encodeURIComponent(token);
    } else if (method === 'POST' || method === 'PUT') {
        // Pour POST et PUT, ajouter le token dans l'URL aussi pour cohérence
        const separator = url.includes('?') ? '&' : '?';
        finalUrl = url + separator + 'token=' + encodeURIComponent(token);
    }
    
    const response = await fetch(finalUrl, finalOptions);
    const text = await response.text();
    
    // Gérer les réponses vides
    if (!text || text.trim() === '') {
        return { status: 'success', code: response.status };
    }
    
    return JSON.parse(text);
}

// ===== FONCTION UTILITAIRE: ESCAPE HTML =====
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}

// ===== FONCTION UTILITAIRE: ENCODE FORM DATA =====
function encodeFormData(formData) {
    if (formData instanceof FormData) {
        return new URLSearchParams(formData);
    }
    if (typeof formData === 'object') {
        return new URLSearchParams(formData);
    }
    return formData;
}
