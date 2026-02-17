<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des Véhicules</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #17245f 0%, #0b0242 100%);
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
            color: #17245f;
            margin-bottom: 30px;
            font-weight: 700;
        }
        .table-custom {
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .table thead {
            background: #17245f;
            color: white;
        }
        .badge-essence {
            background-color: #28a745;
            color: white;
            padding: 6px 12px;
            border-radius: 5px;
            font-weight: 500;
        }
        .badge-diesel {
            background-color: #ffc107;
            color: #333;
            padding: 6px 12px;
            border-radius: 5px;
            font-weight: 500;
        }
        .loading {
            text-align: center;
            padding: 40px;
            color: #17245f;
        }
        .btn-actions {
            display: flex;
            gap: 5px;
        }
    </style>
</head>
<body>
    <div class="container-custom">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="page-title mb-0">🚗 Gestion des Véhicules</h2>
            <div>
                <a href="${pageContext.request.contextPath}/pages/formulaire-vehicule" class="btn btn-primary">
                    ➕ Nouveau Véhicule
                </a>
                <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-secondary">
                    Accueil
                </a>
            </div>
        </div>

        <div id="loading" class="loading">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
            </div>
            <p class="mt-3">Chargement des véhicules...</p>
        </div>

        <div id="error-message" class="alert alert-danger" style="display: none;"></div>

        <div id="vehicules-container" style="display: none;">
            <div class="table-responsive">
                <table class="table table-hover table-custom">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Référence</th>
                            <th>Places</th>
                            <th>Carburant</th>
                            <th>Actions</th>
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
                    <h5 class="modal-title" id="editModalLabel">Modifier le véhicule</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <form id="editForm">
                        <input type="hidden" id="edit_id">
                        <div class="mb-3">
                            <label for="edit_reference" class="form-label">Référence</label>
                            <input type="text" class="form-control" id="edit_reference" required>
                        </div>
                        <div class="mb-3">
                            <label for="edit_place" class="form-label">Nombre de places</label>
                            <input type="number" class="form-control" id="edit_place" min="1" max="50" required>
                        </div>
                        <div class="mb-3">
                            <label for="edit_type_carburant" class="form-label">Type de carburant</label>
                            <select class="form-select" id="edit_type_carburant" required>
                                <option value="E">Essence</option>
                                <option value="D">Diesel</option>
                            </select>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                    <button type="button" class="btn btn-primary" onclick="saveEdit()">Enregistrer</button>
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
                        errorDiv.textContent = message;
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
                tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4">Aucun véhicule trouvé</td></tr>';
            } else {
                vehicules.forEach(vehicule => {
                    const row = document.createElement('tr');
                    
                    const carburantBadge = vehicule.typeCarburant === 'E' 
                        ? 'Essence'
                        : 'Diesel';
                    const badgeClass = vehicule.typeCarburant === 'E' 
                        ? 'badge-essence'
                        : 'badge-diesel';

                    row.innerHTML = '' +
                        '<td>' + escapeHtml(vehicule.id) + '</td>' +
                        '<td><strong>' + escapeHtml(vehicule.reference) + '</strong></td>' +
                        '<td>' + escapeHtml(vehicule.place) + ' places</td>' +
                        '<td><span class="' + badgeClass + '">' + escapeHtml(carburantBadge) + '</span></td>' +
                        '<td>' +
                            '<div class="btn-actions">' +
                                '<button class="btn btn-sm btn-warning" data-id="' + vehicule.id + '" data-action="edit">' +
                                    '✏️ Modifier' +
                                '</button>' +
                                '<button class="btn btn-sm btn-danger" data-id="' + vehicule.id + '" data-action="delete">' +
                                    '🗑️ Supprimer' +
                                '</button>' +
                            '</div>' +
                        '</td>';

                    // Ajouter les event listeners
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
                        alert('Véhicule modifié avec succès');
                    },
                    (code, message) => alert('Erreur: ' + message)
                );
            } catch (error) {
                alert('Erreur: ' + error.message);
            }
        }

        async function deleteVehicule(id, reference) {
            if (!confirm(`Êtes-vous sûr de vouloir supprimer le véhicule ${reference} ?`)) {
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
                        alert('Véhicule supprimé avec succès');
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
