<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nouveau Vehicule</title>
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
        }
        body {
            background: var(--light-bg);
            min-height: 100vh;
            padding: 40px 20px;
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
        }
        .form-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.08);
            padding: 40px;
            max-width: 600px;
            margin: 0 auto;
            border: 1px solid var(--border-color);
        }
        .form-title {
            color: var(--dark);
            font-weight: 700;
            font-size: 1.4rem;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .form-title i {
            color: var(--primary-red);
        }
        .form-label {
            font-weight: 600;
            color: var(--dark);
            font-size: 0.9rem;
        }
        .form-control, .form-select {
            border: 1px solid var(--border-color);
            border-radius: 10px;
            padding: 12px 16px;
            transition: all 0.2s;
        }
        .form-control:focus, .form-select:focus {
            border-color: var(--primary-red);
            box-shadow: 0 0 0 3px rgba(196, 30, 58, 0.1);
        }
        .btn-primary-custom {
            background: var(--primary-red);
            border-color: var(--primary-red);
            color: white;
            padding: 14px 32px;
            font-weight: 600;
            border-radius: 10px;
            font-size: 1rem;
        }
        .btn-primary-custom:hover {
            background: var(--primary-red-dark);
            border-color: var(--primary-red-dark);
            color: white;
        }
        .btn-primary-custom:disabled {
            background: #6c757d;
            border-color: #6c757d;
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
        .alert-custom {
            border-radius: 10px;
        }
        .info-box {
            background: var(--light-bg);
            border-left: 4px solid var(--primary-red);
            padding: 16px;
            border-radius: 0 10px 10px 0;
            margin-bottom: 24px;
        }
        .info-box i {
            color: var(--primary-red);
        }
        .form-text {
            color: #6c757d;
            font-size: 0.8rem;
        }
    </style>
</head>
<body>
    <div class="form-card">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="form-title mb-0">
                <i class="fas fa-car"></i>
                Nouveau Vehicule
            </h2>
            <a href="${pageContext.request.contextPath}/pages/liste-vehicules" class="btn btn-outline-custom">
                <i class="fas fa-arrow-left me-2"></i>Retour a la liste
            </a>
        </div>

        <div class="info-box">
            <small>
                <i class="fas fa-info-circle me-2"></i>
                <strong>Information :</strong> Remplissez tous les champs pour creer un nouveau vehicule. 
                La reference doit etre unique (ex: VH-001, VH-002...).
            </small>
        </div>

        <div id="alert-container"></div>

        <form id="vehiculeForm">
            <div class="mb-3">
                <label for="reference" class="form-label">
                    <i class="fas fa-tag me-2" style="color: var(--primary-red);"></i>Reference du vehicule
                </label>
                <input 
                    type="text" 
                    class="form-control" 
                    id="reference" 
                    placeholder="Ex: VH-100" 
                    required
                    pattern="[A-Z0-9\-]+"
                    title="Utilisez des lettres majuscules, chiffres et tirets uniquement">
                <small class="form-text">Format recommande : VH-XXX</small>
            </div>

            <div class="mb-3">
                <label for="place" class="form-label">
                    <i class="fas fa-users me-2" style="color: var(--primary-red);"></i>Nombre de places
                </label>
                <input 
                    type="number" 
                    class="form-control" 
                    id="place" 
                    min="1" 
                    max="50" 
                    value="5" 
                    required>
                <small class="form-text">Entre 1 et 50 places</small>
            </div>

            <div class="mb-4">
                <label for="type_carburant" class="form-label">
                    <i class="fas fa-gas-pump me-2" style="color: var(--primary-red);"></i>Type de carburant
                </label>
                <select class="form-select" id="type_carburant" required>
                    <option value="">-- Selectionnez un type --</option>
                    <option value="E">Essence (E)</option>
                    <option value="D">Diesel (D)</option>
                </select>
            </div>

            <div class="d-grid">
                <button type="submit" class="btn btn-primary-custom" id="submitBtn">
                    <i class="fas fa-plus me-2"></i>Creer le vehicule
                </button>
            </div>
        </form>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
    <script>
        const BASE_URL = '${pageContext.request.contextPath}';

        document.getElementById('vehiculeForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Creation en cours...';

            const formData = new URLSearchParams({
                reference: document.getElementById('reference').value.toUpperCase(),
                place: document.getElementById('place').value,
                type_carburant: document.getElementById('type_carburant').value
            });

            try {
                const data = await fetchApi(BASE_URL + '/vehicules', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: formData
                });

                const alertContainer = document.getElementById('alert-container');

                handleApiResponse(data,
                    (vehiculeData) => {
                        alertContainer.innerHTML = '' +
                            '<div class="alert alert-success alert-custom" role="alert">' +
                                '<i class="fas fa-check-circle me-2"></i><strong>Succes !</strong> Le vehicule a ete cree avec succes.' +
                                '<br><small>Reference: ' + escapeHtml(vehiculeData.reference) + ' | Places: ' + escapeHtml(vehiculeData.place) + ' | ID: ' + escapeHtml(vehiculeData.id) + '</small>' +
                            '</div>';
                        
                        document.getElementById('vehiculeForm').reset();
                        
                        setTimeout(() => {
                            window.location.href = BASE_URL + '/pages/liste-vehicules';
                        }, 2000);
                    },
                    (code, message) => {
                        alertContainer.innerHTML = '' +
                            '<div class="alert alert-danger alert-custom" role="alert">' +
                                '<i class="fas fa-exclamation-circle me-2"></i><strong>Erreur !</strong> ' + escapeHtml(message) +
                            '</div>';
                        submitBtn.disabled = false;
                        submitBtn.innerHTML = '<i class="fas fa-plus me-2"></i>Creer le vehicule';
                    }
                );
            } catch (error) {
                document.getElementById('alert-container').innerHTML = '' +
                    '<div class="alert alert-danger alert-custom" role="alert">' +
                        '<i class="fas fa-exclamation-circle me-2"></i><strong>Erreur de connexion !</strong> ' + escapeHtml(error.message) +
                    '</div>';
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-plus me-2"></i>Creer le vehicule';
            }
        });

        document.getElementById('reference').addEventListener('blur', function(e) {
            this.value = this.value.toUpperCase().trim();
        });
    </script>
</body>
</html>
