<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nouveau Véhicule</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #17245f 0%, #0b0242 100%);
            min-height: 100vh;
            padding: 40px 20px;
        }
        .form-card {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
            padding: 40px;
            max-width: 700px;
            margin: 0 auto;
        }
        .form-title {
            color: #17245f;
            margin-bottom: 30px;
            font-weight: 700;
        }
        .form-label {
            font-weight: 500;
            color: #272727;
        }
        .btn-submit {
            padding: 12px 40px;
            font-weight: 500;
        }
        .alert-custom {
            border-radius: 10px;
        }
        .info-box {
            background: #e7f3ff;
            border-left: 4px solid #17245f;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="form-card">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="form-title mb-0">🚗 Nouveau Véhicule</h2>
            <a href="${pageContext.request.contextPath}/pages/liste-vehicules" class="btn btn-outline-secondary">Retour à la liste</a>
        </div>

        <div class="info-box">
            <small>
                <strong>ℹ️ Information :</strong> Remplissez tous les champs pour créer un nouveau véhicule. 
                La référence doit être unique (ex: VH-001, VH-002...).
            </small>
        </div>

        <div id="alert-container"></div>

        <form id="vehiculeForm">
            <div class="mb-3">
                <label for="reference" class="form-label">Référence du véhicule *</label>
                <input 
                    type="text" 
                    class="form-control" 
                    id="reference" 
                    placeholder="Ex: VH-100" 
                    required
                    pattern="[A-Z0-9\-]+"
                    title="Utilisez des lettres majuscules, chiffres et tirets uniquement">
                <small class="form-text text-muted">Format recommandé : VH-XXX</small>
            </div>

            <div class="mb-3">
                <label for="place" class="form-label">Nombre de places *</label>
                <input 
                    type="number" 
                    class="form-control" 
                    id="place" 
                    min="1" 
                    max="50" 
                    value="5" 
                    required>
                <small class="form-text text-muted">Entre 1 et 50 places</small>
            </div>

            <div class="mb-3">
                <label for="type_carburant" class="form-label">Type de carburant *</label>
                <select class="form-select" id="type_carburant" required>
                    <option value="">-- Sélectionnez un type --</option>
                    <option value="E">Essence (E)</option>
                    <option value="D">Diesel (D)</option>
                </select>
            </div>

            <div class="d-grid gap-2">
                <button type="submit" class="btn btn-primary btn-submit" id="submitBtn">
                    ➕ Créer le véhicule
                </button>
            </div>
        </form>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const BASE_URL = '${pageContext.request.contextPath}';

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

        document.getElementById('vehiculeForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;
            submitBtn.textContent = '⏳ Création en cours...';

            const formData = new URLSearchParams({
                reference: document.getElementById('reference').value.toUpperCase(),
                place: document.getElementById('place').value,
                type_carburant: document.getElementById('type_carburant').value
            });

            try {
                const response = await fetch(BASE_URL + '/vehicules', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: formData
                });

                const text = await response.text();
                const data = JSON.parse(text);

                const alertContainer = document.getElementById('alert-container');

                if (data.status === 'success') {
                    alertContainer.innerHTML = '' +
                        '<div class="alert alert-success alert-custom" role="alert">' +
                            '<strong>✅ Succès !</strong> Le véhicule a été créé avec succès.' +
                            '<br><small>Référence: ' + escapeHtml(data.data.reference) + ' | Places: ' + escapeHtml(data.data.place) + ' | ID: ' + escapeHtml(data.data.id) + '</small>' +
                        '</div>';
                    
                    // Réinitialiser le formulaire
                    document.getElementById('vehiculeForm').reset();
                    
                    // Rediriger après 2 secondes
                    setTimeout(() => {
                        window.location.href = BASE_URL + '/pages/liste-vehicules';
                    }, 2000);
                } else {
                    alertContainer.innerHTML = '' +
                        '<div class="alert alert-danger alert-custom" role="alert">' +
                            '<strong>❌ Erreur !</strong> ' + escapeHtml(data.message) +
                        '</div>';
                    submitBtn.disabled = false;
                    submitBtn.textContent = '➕ Créer le véhicule';
                }
            } catch (error) {
                document.getElementById('alert-container').innerHTML = '' +
                    '<div class="alert alert-danger alert-custom" role="alert">' +
                        '<strong>❌ Erreur de connexion !</strong> ' + escapeHtml(error.message) +
                    '</div>';
                submitBtn.disabled = false;
                submitBtn.textContent = '➕ Créer le véhicule';
            }
        });

        // Auto-complétion de la référence
        document.getElementById('reference').addEventListener('blur', function(e) {
            this.value = this.value.toUpperCase().trim();
        });
    </script>
</body>
</html>
