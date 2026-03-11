<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des Vehicules</title>
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
            --warning-yellow: #ffc107;
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
            box-shadow: 0 4px 24px rgba(0,0,0,0.08);
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
        .badge-essence {
            background-color: var(--success-green);
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 600;
            font-size: 0.8rem;
        }
        .badge-diesel {
            background-color: var(--warning-yellow);
            color: var(--dark);
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 600;
            font-size: 0.8rem;
        }
        .loading {
            text-align: center;
            padding: 60px;
            color: var(--dark-secondary);
        }
        .loading i {
            color: var(--primary-red);
        }
        .btn-actions {
            display: flex;
            gap: 8px;
        }
        .btn-action-edit {
            background: var(--dark);
            border-color: var(--dark);
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 0.8rem;
        }
        .btn-action-edit:hover {
            background: var(--dark-secondary);
            color: white;
        }
        .btn-action-delete {
            background: var(--primary-red);
            border-color: var(--primary-red);
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 0.8rem;
        }
        .btn-action-delete:hover {
            background: var(--primary-red-dark);
            color: white;
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
        .modal-header {
            background: var(--dark);
            color: white;
            border-radius: 0;
        }
        .modal-title {
            font-weight: 600;
        }
        .btn-close {
            filter: invert(1);
        }
    </style>
</head>
<body>
    <div class="container-custom">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="page-title mb-0">
                <i class="fas fa-car"></i>
                Gestion des Vehicules
            </h2>
            <div>
                <a href="${pageContext.request.contextPath}/pages/formulaire-vehicule" class="btn btn-primary-custom me-2">
                    <i class="fas fa-plus me-2"></i>Nouveau Vehicule
                </a>
                <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-custom">
                    <i class="fas fa-home me-2"></i>Accueil
                </a>
            </div>
        </div>

        <div id="loading" class="loading">
            <i class="fas fa-spinner fa-spin fa-2x mb-3"></i>
            <p>Chargement des vehicules...</p>
        </div>

        <div id="error-message" class="alert alert-danger" style="display: none;"></div>

        <div id="vehicules-container" style="display: none;">
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th><i class="fas fa-hashtag me-2"></i>ID</th>
                            <th><i class="fas fa-tag me-2"></i>Reference</th>
                            <th><i class="fas fa-users me-2"></i>Places</th>
                            <th><i class="fas fa-gas-pump me-2"></i>Carburant</th>
                            <th><i class="fas fa-cogs me-2"></i>Actions</th>
                        </tr>
                    </thead>
                    <tbody id="vehicules-tbody">
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Modal de modification -->
    <div class="modal fade" id="editModal" tabindex="-1" aria-labelledby="editModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="editModalLabel">
                        <i class="fas fa-edit me-2"></i>Modifier le vehicule
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <form id="editForm">
                        <input type="hidden" id="edit_id">
                        <div class="mb-3">
                            <label for="edit_reference" class="form-label fw-bold">Reference</label>
                            <input type="text" class="form-control" id="edit_reference" required>
                        </div>
                        <div class="mb-3">
                            <label for="edit_place" class="form-label fw-bold">Nombre de places</label>
                            <input type="number" class="form-control" id="edit_place" min="1" max="50" required>
                        </div>
                        <div class="mb-3">
                            <label for="edit_type_carburant" class="form-label fw-bold">Type de carburant</label>
                            <select class="form-select" id="edit_type_carburant" required>
                                <option value="E">Essence</option>
                                <option value="D">Diesel</option>
                            </select>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-custom" data-bs-dismiss="modal">Annuler</button>
                    <button type="button" class="btn btn-primary-custom" onclick="saveEdit()">
                        <i class="fas fa-save me-2"></i>Enregistrer
                    </button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        let editModal;

        document.addEventListener('DOMContentLoaded', function() {
            editModal = new bootstrap.Modal(document.getElementById('editModal'));
            loadVehicules();
        });

        async function loadVehicules(url = contextPath + '/vehicules') {
            try {
                const data = await fetchApi(url);
                document.getElementById('loading').style.display = 'none';
                
                handleApiResponse(data, 
                    (vehicules) => displayVehicules(vehicules),
                    (code, message) => {
                        const errorDiv = document.getElementById('error-message');
                        errorDiv.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i>' + message;
                        errorDiv.style.display = 'block';
                    }
                );
            } catch (error) {
                console.error('Error fetching vehicules:', error);
                document.getElementById('loading').style.display = 'none';
                showApiError(500, 'Erreur de connexion: ' + error.message);
            }
        }

        function displayVehicules(vehicules) {
            const tbody = document.getElementById('vehicules-tbody');
            tbody.innerHTML = '';

            if (vehicules.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4"><i class="fas fa-car-side text-muted me-2"></i>Aucun vehicule trouve</td></tr>';
            } else {
                vehicules.forEach(vehicule => {
                    const row = document.createElement('tr');
                    
                    const carburantBadge = vehicule.typeCarburant === 'E' 
                        ? 'Essence'
                        : 'Diesel';
                    const badgeClass = vehicule.typeCarburant === 'E' 
                        ? 'badge-essence'
                        : 'badge-diesel';
                    const fuelIcon = vehicule.typeCarburant === 'E' 
                        ? '<i class="fas fa-leaf me-1"></i>'
                        : '<i class="fas fa-oil-can me-1"></i>';

                    row.innerHTML = '' +
                        '<td>' + escapeHtml(vehicule.id) + '</td>' +
                        '<td><code style="font-weight:600; color: var(--dark);">' + escapeHtml(vehicule.reference) + '</code></td>' +
                        '<td>' + escapeHtml(vehicule.place) + ' <i class="fas fa-user text-muted" style="font-size: 0.75rem;"></i></td>' +
                        '<td><span class="' + badgeClass + '">' + fuelIcon + escapeHtml(carburantBadge) + '</span></td>' +
                        '<td>' +
                            '<div class="btn-actions">' +
                                '<button class="btn btn-action-edit" data-id="' + vehicule.id + '" data-action="edit">' +
                                    '<i class="fas fa-edit me-1"></i>Modifier' +
                                '</button>' +
                                '<button class="btn btn-action-delete" data-id="' + vehicule.id + '" data-action="delete">' +
                                    '<i class="fas fa-trash me-1"></i>Supprimer' +
                                '</button>' +
                            '</div>' +
                        '</td>';

                    const editBtn = row.querySelector('[data-action="edit"]');
                    const deleteBtn = row.querySelector('[data-action="delete"]');
                    
                    editBtn.addEventListener('click', () => {
                        openEditModal(vehicule.id, vehicule.reference, vehicule.place, vehicule.typeCarburant);
                    });
                    
                    deleteBtn.addEventListener('click', () => {
                        deleteVehicule(vehicule.id, vehicule.reference);
                    });

                    tbody.appendChild(row);
                });
            }

            document.getElementById('vehicules-container').style.display = 'block';
        }

        function openEditModal(id, reference, place, typeCarburant) {
            document.getElementById('edit_id').value = id;
            document.getElementById('edit_reference').value = reference;
            document.getElementById('edit_place').value = place;
            document.getElementById('edit_type_carburant').value = typeCarburant;
            editModal.show();
        }

        async function saveEdit(url = contextPath + '/vehicules/update') {
            const formData = new URLSearchParams({
                id: document.getElementById('edit_id').value,
                reference: document.getElementById('edit_reference').value,
                place: document.getElementById('edit_place').value,
                type_carburant: document.getElementById('edit_type_carburant').value
            });

            try {
                const data = await fetchApi(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });

                handleApiResponse(data,
                    () => {
                        editModal.hide();
                        loadVehicules();
                        alert('Vehicule modifie avec succes');
                    },
                    (code, message) => alert('Erreur: ' + message)
                );
            } catch (error) {
                alert('Erreur: ' + error.message);
            }
        }

        async function deleteVehicule(id, reference) {
            if (!confirm('Etes-vous sur de vouloir supprimer le vehicule ' + reference + ' ?')) {
                return;
            }

            const formData = new URLSearchParams({ id: id });

            try {
                const data = await fetchApi(contextPath + '/vehicules/delete', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });

                handleApiResponse(data,
                    () => {
                        loadVehicules();
                        alert('Vehicule supprime avec succes');
                    },
                    (code, message) => alert('Erreur: ' + message)
                );
            } catch (error) {
                console.error('Error deleting vehicule:', error);
                alert('Erreur: ' + error.message);
            }
        }
    </script>
</body>
</html>
