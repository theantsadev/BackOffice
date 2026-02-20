<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Réservations Non Assignées</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #5a5a5a 0%, #111111 100%);
            min-height: 100vh;
            padding: 40px 20px;
        }
        .container-custom {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
            padding: 40px;
            max-width: 1400px;
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
        .loading {
            text-align: center;
            padding: 40px;
            color: #17245f;
        }
        .btn-assign {
            padding: 5px 15px;
        }
    </style>
</head>
<body>
    <div class="container-custom">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="page-title mb-0">Réservations Non Assignées</h2>
            <div>
                <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-secondary">Accueil</a>
            </div>
        </div>

        <div id="loadingDiv" class="loading">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
            </div>
            <p class="mt-2">Chargement des réservations...</p>
        </div>

        <div id="errorDiv" class="alert alert-danger" style="display: none;"></div>
        <div id="successDiv" class="alert alert-success" style="display: none;"></div>

        <div id="tableContainer" style="display: none;">
            <div class="table-responsive">
                <table class="table table-hover table-custom">
                    <thead>
                        <tr>
                            <th>ID Réservation</th>
                            <th>ID Client</th>
                            <th>Nb Passagers</th>
                            <th>Date/Heure Arrivée</th>
                            <th>Hôtel</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody id="reservationTableBody">
                    </tbody>
                </table>
            </div>
        </div>

        <div id="emptyDiv" class="text-center py-5" style="display: none;">
            <h4 class="text-muted">Toutes les réservations sont assignées</h4>
            <p class="text-muted">Aucune réservation en attente de planification</p>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
    <script>
        // Récupérer le token depuis localStorage ou sessionStorage
        const token = localStorage.getItem('api_token') || sessionStorage.getItem('api_token') || '';
        
        async function loadReservations() {
                const loadingDiv = document.getElementById('loadingDiv');
                const errorDiv = document.getElementById('errorDiv');
                const tableContainer = document.getElementById('tableContainer');
                const emptyDiv = document.getElementById('emptyDiv');

                // Vérifier la présence du token côté client
                if (!token || token.trim() === '') {
                    loadingDiv.style.display = 'none';
                    tableContainer.style.display = 'none';
                    emptyDiv.style.display = 'none';
                    errorDiv.innerHTML = 'Token manquant. Générez ou collez un token dans <a href="' +
                        '${pageContext.request.contextPath}/pages/gestion-tokens">Gestion Tokens</a> puis rechargez la page.';
                    errorDiv.style.display = 'block';
                    return;
                }

                loadingDiv.style.display = 'block';
                errorDiv.style.display = 'none';
                tableContainer.style.display = 'none';
                emptyDiv.style.display = 'none';
            
            try {
                const data = await fetchApi('${pageContext.request.contextPath}/reservations/non-assignees');
                console.log('Réservations non assignées (raw):', data);
                loadingDiv.style.display = 'none';

                handleApiResponse(data, (reservations) => {
                    reservations = reservations || [];
                    if (reservations.length === 0) {
                        emptyDiv.style.display = 'block';
                        return;
                    }

                    const tbody = document.getElementById('reservationTableBody');
                    tbody.innerHTML = '';

                    reservations.forEach(r => {
                        const dateArrivee = new Date(r.date_heure_arrivee).toLocaleString('fr-FR');

                        const row = document.createElement('tr');
                        row.id = 'row-' + r.id_reservation;
                        row.innerHTML = `
                            <td>${r.id_reservation}</td>
                            <td>${r.id_client}</td>
                            <td>${r.nb_passager}</td>
                            <td>${dateArrivee}</td>
                            <td>${r.nom_hotel || '-'}</td>
                            <td>
                                <button class="btn btn-primary btn-assign" onclick="assignerReservation(${r.id_reservation})">
                                    Assigner
                                </button>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });

                    tableContainer.style.display = 'block';
                }, (code, message) => {
                    errorDiv.innerHTML = message;
                    errorDiv.style.display = 'block';
                });

            } catch (error) {
                loadingDiv.style.display = 'none';
                errorDiv.textContent = 'Erreur de connexion: ' + error.message;
                errorDiv.style.display = 'block';
            }
        }
        
        async function assignerReservation(idReservation) {
            const errorDiv = document.getElementById('errorDiv');
            const successDiv = document.getElementById('successDiv');
            
            errorDiv.style.display = 'none';
            successDiv.style.display = 'none';
            
            // Désactiver le bouton pendant le traitement
            const button = document.querySelector('#row-' + idReservation + ' button');
            button.disabled = true;
            button.textContent = 'En cours...';
            
            try {
                const formData = new FormData();
                formData.append('token', token);
                formData.append('id_reservation', idReservation);
                
                const data = await fetchApi('${pageContext.request.contextPath}/planifications', { method: 'POST', body: formData });

                handleApiResponse(data, (planification) => {
                    // Succès - retirer la ligne du tableau
                    successDiv.textContent = 'Réservation ' + idReservation + ' assignée avec succès !';
                    successDiv.style.display = 'block';

                    document.getElementById('row-' + idReservation).remove();
                }, (code, message) => {
                    errorDiv.textContent = 'Erreur pour la réservation ' + idReservation + ': ' + message;
                    errorDiv.style.display = 'block';
                    button.disabled = false;
                    button.textContent = 'Assigner';
                    return;
                });
                
                // Vérifier s'il reste des réservations
                const tbody = document.getElementById('reservationTableBody');
                if (tbody.children.length === 0) {
                    document.getElementById('tableContainer').style.display = 'none';
                    document.getElementById('emptyDiv').style.display = 'block';
                }
                
                // Cacher le message de succès après 3 secondes
                setTimeout(() => {
                    successDiv.style.display = 'none';
                }, 3000);
                
            } catch (error) {
                errorDiv.textContent = 'Erreur de connexion: ' + error.message;
                errorDiv.style.display = 'block';
                button.disabled = false;
                button.textContent = 'Assigner';
            }
        }
        
        // Charger les réservations au chargement de la page
        document.addEventListener('DOMContentLoaded', loadReservations);
    </script>
</body>
</html>
