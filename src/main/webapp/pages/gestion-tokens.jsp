<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestion des Tokens</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 40px 20px;
        }
        .container-custom {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
            padding: 40px;
            max-width: 1200px;
            margin: 0 auto;
        }
        .page-title {
            color: #667eea;
            margin-bottom: 30px;
            font-weight: 700;
        }
        .action-section {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        .table-custom {
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .table thead {
            background: #667eea;
            color: white;
        }
        .badge-valid {
            background-color: #28a745;
            color: white;
            padding: 6px 12px;
            border-radius: 5px;
            font-weight: 500;
        }
        .badge-expired {
            background-color: #dc3545;
            color: white;
            padding: 6px 12px;
            border-radius: 5px;
            font-weight: 500;
        }
        .token-preview {
            font-family: monospace;
            font-size: 12px;
            background: #f1f3f5;
            padding: 4px 8px;
            border-radius: 4px;
        }
        .loading {
            text-align: center;
            padding: 40px;
            color: #667eea;
        }
        .generate-form {
            display: flex;
            gap: 10px;
            align-items: center;
        }
        .info-box {
            background: #e7f3ff;
            border-left: 4px solid #667eea;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container-custom">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="page-title mb-0">🔐 Gestion des Tokens</h2>
            <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-secondary">Accueil</a>
        </div>

        <div class="info-box">
            <strong>ℹ️ À propos des tokens :</strong> Les tokens sont des identifiants uniques avec une date d'expiration. 
            Ils peuvent être utilisés pour l'authentification ou l'autorisation d'API.
        </div>

        <!-- Section Génération -->
        <div class="action-section">
            <h5>➕ Générer un nouveau token</h5>
            <div class="generate-form">
                <label>Expiration dans :</label>
                <input type="number" id="expiration_minutes" class="form-control" style="max-width: 120px;" value="60" min="1">
                <span>minutes</span>
                <button class="btn btn-primary" onclick="generateToken()">Générer</button>
            </div>
            <small class="text-muted d-block mt-2">
                Exemples : 60 min = 1 heure | 1440 min = 1 jour | 10080 min = 7 jours
            </small>
        </div>

        <!-- Section Validation -->
        <div class="action-section">
            <h5>✅ Valider un token</h5>
            <div class="generate-form">
                <input type="text" id="token_to_validate" class="form-control" placeholder="Collez le token ici..." style="flex: 1;">
                <button class="btn btn-success" onclick="validateToken()">Valider</button>
            </div>
            <div id="validation-result" class="mt-3"></div>
        </div>

        <!-- Section Nettoyage -->
        <div class="action-section">
            <h5>🧹 Nettoyer les tokens expirés</h5>
            <button class="btn btn-warning" onclick="cleanupExpiredTokens()">Supprimer tous les tokens expirés</button>
            <div id="cleanup-result" class="mt-3"></div>
        </div>

        <div id="loading" class="loading">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
            </div>
            <p class="mt-3">Chargement des tokens...</p>
        </div>

        <div id="error-message" class="alert alert-danger" style="display: none;"></div>

        <div id="tokens-container" style="display: none;">
            <h5 class="mb-3">📋 Liste des tokens</h5>
            <div class="table-responsive">
                <table class="table table-hover table-custom">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Token</th>
                            <th>Date d'expiration</th>
                            <th>Status</th>
                            <th>Actions</th>
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
                    <h5 class="modal-title" id="tokenModalLabel">Token complet</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fw-bold">Token :</label>
                        <textarea class="form-control" id="full-token" rows="3" readonly></textarea>
                    </div>
                    <button class="btn btn-sm btn-outline-primary" onclick="copyToClipboard()">📋 Copier</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const BASE_URL = '${pageContext.request.contextPath}';
        let tokenModal;

        document.addEventListener('DOMContentLoaded', function() {
            tokenModal = new bootstrap.Modal(document.getElementById('tokenModal'));
            loadTokens();
        });

        async function loadTokens() {
            try {
                const response = await fetch(`${BASE_URL}/tokens`);
                const text = await response.text();
                const data = JSON.parse(text);

                document.getElementById('loading').style.display = 'none';

                if (data.status === 'success') {
                    displayTokens(data.data);
                } else {
                    showError('Erreur lors du chargement: ' + data.message);
                }
            } catch (error) {
                document.getElementById('loading').style.display = 'none';
                showError('Erreur de connexion: ' + error.message);
            }
        }

        function displayTokens(tokens) {
            const tbody = document.getElementById('tokens-tbody');
            tbody.innerHTML = '';

            if (tokens.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4">Aucun token trouvé. Générez-en un ci-dessus.</td></tr>';
            } else {
                tokens.forEach(token => {
                    const isExpired = new Date(token.dateHeureExpiration) < new Date();
                    const statusBadge = isExpired 
                        ? '<span class="badge-expired">EXPIRÉ</span>'
                        : '<span class="badge-valid">VALIDE</span>';

                    const tokenPreview = token.token.substring(0, 20) + '...';
                    const formattedDate = new Date(token.dateHeureExpiration).toLocaleString('fr-FR');

                    const row = '<tr>' +
                        '<td>' + token.id + '</td>' +
                        '<td>' +
                            '<span class="token-preview">' + tokenPreview + '</span>' +
                            '<button class="btn btn-sm btn-link" onclick="showFullToken(\'' + token.token + '\')">Voir tout</button>' +
                        '</td>' +
                        '<td>' + formattedDate + '</td>' +
                        '<td>' + statusBadge + '</td>' +
                        '<td>' +
                            '<button class="btn btn-sm btn-danger" onclick="deleteToken(' + token.id + ')">🗑️ Supprimer</button>' +
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

                const response = await fetch(`${BASE_URL}/tokens/generate`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });

                const text = await response.text();
                const data = JSON.parse(text);

                if (data.status === 'success') {
                    var formattedDate = new Date(data.data.dateHeureExpiration).toLocaleString('fr-FR');
                    alert('✅ Token généré avec succès !\n\nToken: ' + data.data.token.substring(0, 30) + '...\nExpiration: ' + formattedDate);
                    loadTokens();
                } else {
                    alert('❌ Erreur: ' + data.message);
                }
            } catch (error) {
                alert('❌ Erreur: ' + error.message);
            }
        }

        async function validateToken() {
            const tokenValue = document.getElementById('token_to_validate').value.trim();
            const resultDiv = document.getElementById('validation-result');

            if (!tokenValue) {
                resultDiv.innerHTML = '<div class="alert alert-warning">⚠️ Veuillez entrer un token</div>';
                return;
            }

            try {
                const formData = new URLSearchParams({ token: tokenValue });

                const response = await fetch(`${BASE_URL}/tokens/validate`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });

                const text = await response.text();
                const data = JSON.parse(text);

                if (data.status === 'success') {
                    resultDiv.innerHTML = '<div class="alert alert-success">&#x2705; ' + data.data + '</div>';
                } else {
                    resultDiv.innerHTML = '<div class="alert alert-danger">&#x274C; ' + data.message + '</div>';
                }
            } catch (error) {
                resultDiv.innerHTML = '<div class="alert alert-danger">❌ Erreur: ' + error.message + '</div>';
            }
        }

        async function cleanupExpiredTokens() {
            if (!confirm('Êtes-vous sûr de vouloir supprimer tous les tokens expirés ?')) {
                return;
            }

            try {
                const response = await fetch(`${BASE_URL}/tokens/cleanup`, {
                    method: 'POST'
                });

                const text = await response.text();
                const data = JSON.parse(text);

                const resultDiv = document.getElementById('cleanup-result');

                if (data.status === 'success') {
                    resultDiv.innerHTML = '<div class="alert alert-success">&#x2705; ' + data.data + '</div>';
                    loadTokens();
                } else {
                    resultDiv.innerHTML = '<div class="alert alert-danger">&#x274C; ' + data.message + '</div>';
                }
            } catch (error) {
                document.getElementById('cleanup-result').innerHTML = '<div class="alert alert-danger">❌ Erreur: ' + error.message + '</div>';
            }
        }

        async function deleteToken(id) {
            if (!confirm('Êtes-vous sûr de vouloir supprimer ce token ?')) {
                return;
            }

            const formData = new URLSearchParams({ id: id });

            try {
                const response = await fetch(`${BASE_URL}/tokens/delete`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });

                const text = await response.text();
                const data = JSON.parse(text);

                if (data.status === 'success') {
                    loadTokens();
                    alert('Token supprimé avec succès');
                } else {
                    alert('Erreur: ' + data.message);
                }
            } catch (error) {
                alert('❌ Erreur: ' + error.message);
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
            alert('Token copié dans le presse-papiers !');
        }

        function showError(message) {
            const errorDiv = document.getElementById('error-message');
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
        }
    </script>
</body>
</html>
