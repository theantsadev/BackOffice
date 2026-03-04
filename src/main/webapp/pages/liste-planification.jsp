<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Résultat Planification Automatique</title>
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
        .filter-section {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        .table-custom {
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .table thead {
            background: #17245f;
            color: white;
        }
        .table-warning-header thead {
            background: #856404;
            color: white;
        }
        .loading {
            text-align: center;
            padding: 40px;
            color: #17245f;
        }
        .date-display {
            font-size: 1.2rem;
            color: #17245f;
            font-weight: 600;
        }
        .section-title {
            color: #17245f;
            font-weight: 600;
            margin-top: 30px;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #17245f;
        }
        .section-title-warning {
            color: #856404;
            border-bottom-color: #856404;
        }
        .badge-count {
            font-size: 0.9rem;
            margin-left: 10px;
        }
    </style>
</head>
<body>
    <div class="container-custom">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="page-title mb-0">Résultat Planification Automatique</h2>
            <div>
                <a href="${pageContext.request.contextPath}/pages/formulaire-planification" class="btn btn-primary me-2">Nouvelle Recherche</a>
                <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-secondary">Accueil</a>
            </div>
        </div>

        <div class="filter-section">
            <div class="row align-items-center">
                <div class="col-md-4">
                    <span class="date-display">📅 Date: <span id="selectedDate"></span></span>
                </div>
                <div class="col-md-8 text-end">
                    <span id="countAssigned" class="badge bg-success badge-count"></span>
                    <span id="countNonAssigned" class="badge bg-warning text-dark badge-count"></span>
                </div>
            </div>
        </div>

        <div id="loadingDiv" class="loading">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
            </div>
            <p class="mt-2">Planification automatique en cours...</p>
        </div>

        <div id="errorDiv" class="alert alert-danger" style="display: none;"></div>

        <!-- Section Planifications Assignées -->
        <div id="planificationSection" style="display: none;">
            <h4 class="section-title">✅ Planifications Assignées <span id="planifCount" class="badge bg-success badge-count"></span></h4>
            <div class="table-responsive">
                <table class="table table-hover table-custom">
                    <thead>
                        <tr>
                            <th>ID Planification</th>
                            <th>ID Réservation</th>
                            <th>ID Client</th>
                            <th>Hôtel</th>
                            <th>Véhicule</th>
                            <th>Départ Aéroport</th>
                            <th>Retour Aéroport</th>
                        </tr>
                    </thead>
                    <tbody id="planificationTableBody">
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Section Réservations Non Assignées -->
        <div id="nonAssignedSection" style="display: none;">
            <h4 class="section-title section-title-warning">⚠️ Réservations Non Assignées <span id="nonAssignedCount" class="badge bg-warning text-dark badge-count"></span></h4>
            <p class="text-muted">Aucun véhicule disponible n'a été trouvé pour ces réservations.</p>
            <div class="table-responsive">
                <table class="table table-hover table-custom table-warning-header">
                    <thead>
                        <tr>
                            <th>ID Réservation</th>
                            <th>ID Client</th>
                            <th>Nb Passagers</th>
                            <th>Date/Heure Arrivée</th>
                            <th>Hôtel</th>
                        </tr>
                    </thead>
                    <tbody id="nonAssignedTableBody">
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Aucun résultat -->
        <div id="emptyDiv" class="text-center py-5" style="display: none;">
            <h4 class="text-muted">Aucune réservation trouvée pour cette date</h4>
            <p class="text-muted">Essayez une autre date</p>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
    <script>
        const urlParams = new URLSearchParams(window.location.search);
        const date = urlParams.get('date') || new Date().toISOString().split('T')[0];
        
        document.getElementById('selectedDate').textContent = date;
        
        const token = localStorage.getItem('api_token') || sessionStorage.getItem('api_token') || '';
        
        async function loadPlanifications() {
            const loadingDiv = document.getElementById('loadingDiv');
            const errorDiv = document.getElementById('errorDiv');
            const planificationSection = document.getElementById('planificationSection');
            const nonAssignedSection = document.getElementById('nonAssignedSection');
            const emptyDiv = document.getElementById('emptyDiv');
            
            // Vérifier le token
            if (!token || token.trim() === '') {
                loadingDiv.style.display = 'none';
                errorDiv.innerHTML = 'Token manquant. Générez ou collez un token dans <a href="' +
                    '${pageContext.request.contextPath}/pages/gestion-tokens">Gestion Tokens</a> puis rechargez la page.';
                errorDiv.style.display = 'block';
                return;
            }

            loadingDiv.style.display = 'block';
            errorDiv.style.display = 'none';
            planificationSection.style.display = 'none';
            nonAssignedSection.style.display = 'none';
            emptyDiv.style.display = 'none';
            
            try {
                const data = await fetchApi('${pageContext.request.contextPath}/planifications?date=' + date);
                console.log('Résultat planification auto (raw):', data);
                loadingDiv.style.display = 'none';

                handleApiResponse(data, (result) => {
                    const planifications = result.planifications || [];
                    const reservationsNonAssignees = result.reservationsNonAssignees || [];

                    // Compteurs
                    document.getElementById('countAssigned').textContent = planifications.length + ' assignée(s)';
                    document.getElementById('countNonAssigned').textContent = reservationsNonAssignees.length + ' non assignée(s)';

                    // Si rien du tout
                    if (planifications.length === 0 && reservationsNonAssignees.length === 0) {
                        emptyDiv.style.display = 'block';
                        return;
                    }

                    // Afficher les planifications assignées
                    if (planifications.length > 0) {
                        document.getElementById('planifCount').textContent = planifications.length;

                        const tbody = document.getElementById('planificationTableBody');
                        tbody.innerHTML = '';

                        planifications.forEach(function(p) {
                            var depart = new Date(p.dateHeureDepartAeroport).toLocaleString('fr-FR');
                            var retour = new Date(p.dateHeureRetourAeroport).toLocaleString('fr-FR');

                            var row = document.createElement('tr');
                            row.innerHTML = '' +
                                '<td>' + escapeHtml(p.idPlanification) + '</td>' +
                                '<td>' + escapeHtml(p.idReservation) + '</td>' +
                                '<td>' + escapeHtml(p.idClient || '-') + '</td>' +
                                '<td>' + escapeHtml(p.nomHotel || '-') + '</td>' +
                                '<td>' + escapeHtml(p.referenceVehicule || p.idVehicule) + '</td>' +
                                '<td>' + escapeHtml(depart) + '</td>' +
                                '<td>' + escapeHtml(retour) + '</td>';
                            tbody.appendChild(row);
                        });

                        planificationSection.style.display = 'block';
                    }

                    // Afficher les réservations non assignées
                    if (reservationsNonAssignees.length > 0) {
                        document.getElementById('nonAssignedCount').textContent = reservationsNonAssignees.length;

                        var tbody2 = document.getElementById('nonAssignedTableBody');
                        tbody2.innerHTML = '';

                        reservationsNonAssignees.forEach(function(r) {
                            var dateArrivee = new Date(r.date_heure_arrivee).toLocaleString('fr-FR');

                            var row = document.createElement('tr');
                            row.innerHTML = '' +
                                '<td>' + escapeHtml(r.id_reservation) + '</td>' +
                                '<td>' + escapeHtml(r.id_client || '-') + '</td>' +
                                '<td>' + escapeHtml(r.nb_passager) + '</td>' +
                                '<td>' + escapeHtml(dateArrivee) + '</td>' +
                                '<td>' + escapeHtml(r.nom_hotel || '-') + '</td>';
                            tbody2.appendChild(row);
                        });

                        nonAssignedSection.style.display = 'block';
                    }

                }, function(code, message) {
                    errorDiv.textContent = message;
                    errorDiv.style.display = 'block';
                });

            } catch (error) {
                loadingDiv.style.display = 'none';
                errorDiv.textContent = 'Erreur de connexion: ' + error.message;
                errorDiv.style.display = 'block';
            }
        }
        
        document.addEventListener('DOMContentLoaded', loadPlanifications);
    </script>
</body>
</html>
