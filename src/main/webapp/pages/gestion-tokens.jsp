<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="fr">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestion des Tokens</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
        <style>
            :root {
                --primary-red: #c41e3a;
                --primary-red-dark: #a01830;
                --dark: #1a1a1a;
                --dark-secondary: #333333;
                --light-bg: #f8f9fa;
                --border-color: #e0e0e0;
                --success-green: #198754;
                --danger-red: #dc3545;
                --warning-orange: #fd7e14;
            }

            body {
                background: var(--light-bg);
                min-height: 100vh;
                padding: 32px 20px;
                font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
            }

            .container-custom {
                background: white;
                border-radius: 16px;
                box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
                padding: 32px;
                max-width: 1200px;
                margin: 0 auto;
                border: 1px solid var(--border-color);
            }

            .page-title {
                color: var(--dark);
                font-weight: 700;
                font-size: 1.4rem;
                display: flex;
                align-items: center;
                gap: 12px;
            }

            .page-title i {
                color: var(--primary-red);
            }

            .action-section {
                background: var(--light-bg);
                padding: 24px;
                border-radius: 12px;
                margin-bottom: 24px;
                border: 1px solid var(--border-color);
            }

            .action-section h5 {
                color: var(--dark);
                font-weight: 600;
                font-size: 0.95rem;
                margin-bottom: 16px;
                display: flex;
                align-items: center;
                gap: 8px;
            }

            .action-section h5 i {
                color: var(--primary-red);
            }

            .table thead {
                background: var(--dark);
                color: white;
            }

            .table thead th {
                font-weight: 600;
                font-size: 0.8rem;
                text-transform: uppercase;
                letter-spacing: 0.3px;
                padding: 14px 12px;
                border: none;
            }

            .table tbody td {
                padding: 14px 12px;
                vertical-align: middle;
                font-size: 0.9rem;
                border-color: var(--border-color);
            }

            .badge-valid {
                background-color: var(--success-green);
                color: white;
                padding: 6px 12px;
                border-radius: 6px;
                font-weight: 600;
                font-size: 0.75rem;
            }

            .badge-expired {
                background-color: var(--danger-red);
                color: white;
                padding: 6px 12px;
                border-radius: 6px;
                font-weight: 600;
                font-size: 0.75rem;
            }

            .token-preview {
                font-family: 'Consolas', monospace;
                font-size: 0.8rem;
                background: var(--light-bg);
                padding: 4px 10px;
                border-radius: 6px;
                color: var(--dark);
            }

            .loading {
                text-align: center;
                padding: 60px;
                color: var(--dark-secondary);
            }

            .loading i {
                color: var(--primary-red);
            }

            .generate-form {
                display: flex;
                gap: 12px;
                align-items: center;
                flex-wrap: wrap;
            }

            .info-box {
                background: white;
                border-left: 4px solid var(--primary-red);
                padding: 16px;
                border-radius: 0 10px 10px 0;
                margin-bottom: 24px;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            }

            .info-box i {
                color: var(--primary-red);
            }

            .btn-primary-custom {
                background: var(--primary-red);
                border-color: var(--primary-red);
                color: white;
                padding: 10px 20px;
                border-radius: 10px;
                font-weight: 500;
            }

            .btn-primary-custom:hover {
                background: var(--primary-red-dark);
                border-color: var(--primary-red-dark);
                color: white;
            }

            .btn-success-custom {
                background: var(--success-green);
                border-color: var(--success-green);
                color: white;
                padding: 10px 20px;
                border-radius: 10px;
                font-weight: 500;
            }

            .btn-success-custom:hover {
                background: #157347;
                border-color: #157347;
                color: white;
            }

            .btn-warning-custom {
                background: var(--warning-orange);
                border-color: var(--warning-orange);
                color: white;
                padding: 10px 20px;
                border-radius: 10px;
                font-weight: 500;
            }

            .btn-warning-custom:hover {
                background: #e56d00;
                border-color: #e56d00;
                color: white;
            }

            .btn-delete {
                background: var(--primary-red);
                border-color: var(--primary-red);
                color: white;
                padding: 6px 12px;
                border-radius: 6px;
                font-size: 0.8rem;
            }

            .btn-delete:hover {
                background: var(--primary-red-dark);
                color: white;
            }

            .btn-outline-custom {
                border-color: var(--border-color);
                color: var(--dark-secondary);
                padding: 10px 20px;
                border-radius: 10px;
            }

            .btn-outline-custom:hover {
                background: var(--dark);
                border-color: var(--dark);
                color: white;
            }

            .form-control {
                border: 1px solid var(--border-color);
                border-radius: 10px;
                padding: 10px 16px;
            }

            .form-control:focus {
                border-color: var(--primary-red);
                box-shadow: 0 0 0 3px rgba(196, 30, 58, 0.1);
            }

            .modal-header {
                background: var(--dark);
                color: white;
            }

            .modal-title {
                font-weight: 600;
            }

            .btn-close {
                filter: invert(1);
            }

            .section-title {
                color: var(--dark);
                font-weight: 700;
                font-size: 1rem;
                margin-bottom: 16px;
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .section-title i {
                color: var(--dark);
            }
        </style>
    </head>

    <body>
        <div class="container-custom">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="page-title mb-0">
                    <i class="fas fa-key"></i>
                    Gestion des Tokens
                </h2>
                <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-custom">
                    <i class="fas fa-home me-2"></i>Accueil
                </a>
            </div>

            <div class="info-box">
                <i class="fas fa-info-circle me-2"></i>
                <strong>A propos des tokens :</strong> Les tokens sont des identifiants uniques avec une date
                d'expiration. Ils peuvent etre utilises pour l'authentification ou l'autorisation d'API.
            </div>

            <!-- Section Generation -->
            <div class="action-section">
                <h5><i class="fas fa-plus-circle"></i> Generer un nouveau token</h5>
                <div class="generate-form">
                    <label class="fw-bold">Expiration dans :</label>
                    <input type="number" id="expiration_minutes" class="form-control" style="max-width: 100px;"
                        value="60" min="1">
                    <span>minutes</span>
                    <button class="btn btn-primary-custom" onclick="generateToken()">
                        <i class="fas fa-key me-2"></i>Generer
                    </button>
                </div>
                <small class="text-muted d-block mt-2">
                    <i class="fas fa-lightbulb me-1"></i>
                    Exemples : 60 min = 1 heure | 1440 min = 1 jour | 10080 min = 7 jours
                </small>
            </div>

            <!-- Section Validation -->
            <div class="action-section">
                <h5><i class="fas fa-check-circle"></i> Valider un token</h5>
                <div class="generate-form">
                    <input type="text" id="token_to_validate" class="form-control" placeholder="Collez le token ici..."
                        style="flex: 1;">
                    <button class="btn btn-success-custom" onclick="validateToken()">
                        <i class="fas fa-search me-2"></i>Valider
                    </button>
                </div>
                <div id="validation-result" class="mt-3"></div>
            </div>

            <!-- Section Nettoyage -->
            <div class="action-section">
                <h5><i class="fas fa-broom"></i> Nettoyer les tokens expires</h5>
                <button class="btn btn-warning-custom" onclick="cleanupExpiredTokens()">
                    <i class="fas fa-trash-alt me-2"></i>Supprimer tous les tokens expires
                </button>
                <div id="cleanup-result" class="mt-3"></div>
            </div>

            <div id="loading" class="loading">
                <i class="fas fa-spinner fa-spin fa-2x mb-3"></i>
                <p>Chargement des tokens...</p>
            </div>

            <div id="error-message" class="alert alert-danger" style="display: none;"></div>

            <div id="tokens-container" style="display: none;">
                <h5 class="section-title">
                    <i class="fas fa-list"></i> Liste des tokens
                </h5>
                <div class="table-responsive">
                    <table class="table table-hover">
                        <thead>
                            <tr>
                                <th><i class="fas fa-hashtag me-2"></i>ID</th>
                                <th><i class="fas fa-key me-2"></i>Token</th>
                                <th><i class="fas fa-calendar me-2"></i>Date d'expiration</th>
                                <th><i class="fas fa-info-circle me-2"></i>Status</th>
                                <th><i class="fas fa-cogs me-2"></i>Actions</th>
                            </tr>
                        </thead>
                        <tbody id="tokens-tbody">
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Modal pour afficher un token complet -->
        <div class="modal fade" id="tokenModal" tabindex="-1" aria-labelledby="tokenModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="tokenModalLabel">
                            <i class="fas fa-key me-2"></i>Token complet
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label fw-bold">Token :</label>
                            <textarea class="form-control" id="full-token" rows="3" readonly
                                style="font-family: 'Consolas', monospace;"></textarea>
                        </div>
                        <button class="btn btn-outline-custom" onclick="copyToClipboard()">
                            <i class="fas fa-copy me-2"></i>Copier
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
        <script>
            const BASE_URL = '${pageContext.request.contextPath}';
            let tokenModal;

            document.addEventListener('DOMContentLoaded', function () {
                tokenModal = new bootstrap.Modal(document.getElementById('tokenModal'));
                loadTokens();
            });

            async function loadTokens() {
                try {
                    const data = await fetchApi(BASE_URL + '/tokens', {}, false);
                    document.getElementById('loading').style.display = 'none';

                    handleApiResponse(data,
                        (tokens) => displayTokens(tokens),
                        (code, message) => {
                            const errorDiv = document.getElementById('error-message');
                            errorDiv.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i>' + message;
                            errorDiv.style.display = 'block';
                        }
                    );
                } catch (error) {
                    document.getElementById('loading').style.display = 'none';
                    const errorDiv = document.getElementById('error-message');
                    errorDiv.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i>Erreur de connexion: ' + error.message;
                    errorDiv.style.display = 'block';
                }
            }

            function displayTokens(tokens) {
                const tbody = document.getElementById('tokens-tbody');
                tbody.innerHTML = '';

                if (tokens.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4"><i class="fas fa-key text-muted me-2"></i>Aucun token trouve. Generez-en un ci-dessus.</td></tr>';
                } else {
                    tokens.forEach(token => {
                        const isExpired = new Date(token.dateHeureExpiration) < new Date();
                        const statusBadge = isExpired
                            ? '<span class="badge-expired"><i class="fas fa-times-circle me-1"></i>EXPIRE</span>'
                            : '<span class="badge-valid"><i class="fas fa-check-circle me-1"></i>VALIDE</span>';

                        const tokenPreview = token.token.substring(0, 20) + '...';
                        const formattedDate = new Date(token.dateHeureExpiration).toLocaleString('fr-FR');

                        const row = '<tr>' +
                            '<td>' + token.id + '</td>' +
                            '<td>' +
                            '<span class="token-preview">' + escapeHtml(tokenPreview) + '</span>' +
                            '<button class="btn btn-sm btn-link p-0 ms-2" onclick="showFullToken(\'' + escapeHtml(token.token) + '\')" style="color: var(--primary-red);">' +
                            '<i class="fas fa-eye me-1"></i>Voir' +
                            '</button>' +
                            '</td>' +
                            '<td><i class="fas fa-clock text-muted me-2" style="font-size: 0.8rem;"></i>' + escapeHtml(formattedDate) + '</td>' +
                            '<td>' + statusBadge + '</td>' +
                            '<td>' +
                            '<button class="btn btn-delete" onclick="deleteToken(' + token.id + ')">' +
                            '<i class="fas fa-trash me-1"></i>Supprimer' +
                            '</button>' +
                            '</td>' +
                            '</tr>';
                        tbody.innerHTML += row;
                    });
                }

                document.getElementById('tokens-container').style.display = 'block';
            }

            async function generateToken() {
                const expirationMinutes = document.getElementById('expiration_minutes').value;

                try {
                    const formData = new URLSearchParams({
                        expiration_minutes: expirationMinutes
                    });

                    const data = await fetchApi(BASE_URL + '/tokens/generate', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: formData
                    }, false);

                    handleApiResponse(data,
                        (tokenData) => {
                            var formattedDate = new Date(tokenData.dateHeureExpiration).toLocaleString('fr-FR');
                            alert('Token genere avec succes !\n\nToken: ' + tokenData.token.substring(0, 30) + '...\nExpiration: ' + formattedDate);
                            loadTokens();
                        },
                        (code, message) => alert('Erreur: ' + message)
                    );
                } catch (error) {
                    alert('Erreur: ' + error.message);
                }
            }

            async function validateToken() {
                const tokenValue = document.getElementById('token_to_validate').value.trim();
                const resultDiv = document.getElementById('validation-result');

                if (!tokenValue) {
                    resultDiv.innerHTML = '<div class="alert alert-warning"><i class="fas fa-exclamation-triangle me-2"></i>Veuillez entrer un token</div>';
                    return;
                }

                try {
                    const formData = new URLSearchParams({ token: tokenValue });

                    const data = await fetchApi(BASE_URL + '/tokens/validate', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: formData
                    }, false);

                    handleApiResponse(data,
                        (message) => {
                            resultDiv.innerHTML = '<div class="alert alert-success"><i class="fas fa-check-circle me-2"></i>' + message + '</div>';
                        },
                        (code, message) => {
                            resultDiv.innerHTML = '<div class="alert alert-danger"><i class="fas fa-times-circle me-2"></i>' + message + '</div>';
                        }
                    );
                } catch (error) {
                    resultDiv.innerHTML = '<div class="alert alert-danger"><i class="fas fa-times-circle me-2"></i>Erreur: ' + error.message + '</div>';
                }
            }

            async function cleanupExpiredTokens() {
                if (!confirm('Etes-vous sur de vouloir supprimer tous les tokens expires ?')) {
                    return;
                }

                try {
                    const data = await fetchApi(BASE_URL + '/tokens/cleanup', {
                        method: 'POST'
                    }, false);

                    const resultDiv = document.getElementById('cleanup-result');

                    handleApiResponse(data,
                        (message) => {
                            resultDiv.innerHTML = '<div class="alert alert-success"><i class="fas fa-check-circle me-2"></i>' + message + '</div>';
                            loadTokens();
                        },
                        (code, message) => {
                            resultDiv.innerHTML = '<div class="alert alert-danger"><i class="fas fa-times-circle me-2"></i>' + message + '</div>';
                        }
                    );
                } catch (error) {
                    document.getElementById('cleanup-result').innerHTML = '<div class="alert alert-danger"><i class="fas fa-times-circle me-2"></i>Erreur: ' + error.message + '</div>';
                }
            }

            async function deleteToken(id) {
                if (!confirm('Etes-vous sur de vouloir supprimer ce token ?')) {
                    return;
                }

                const formData = new URLSearchParams({ id: id });

                try {
                    const data = await fetchApi(BASE_URL + '/tokens/delete', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: formData
                    }, false);

                    handleApiResponse(data,
                        () => {
                            loadTokens();
                            alert('Token supprime avec succes');
                        },
                        (code, message) => alert('Erreur: ' + message)
                    );
                } catch (error) {
                    alert('Erreur: ' + error.message);
                }
            }

            function showFullToken(token) {
                document.getElementById('full-token').value = token;
                tokenModal.show();
            }

            function copyToClipboard() {
                const tokenText = document.getElementById('full-token');
                tokenText.select();
                document.execCommand('copy');
                alert('Token copie dans le presse-papiers !');
            }
        </script>
    </body>

    </html>