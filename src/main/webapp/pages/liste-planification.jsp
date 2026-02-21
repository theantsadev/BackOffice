<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des Planifications</title>
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
    </style>
</head>
<body>
    <div class="container-custom">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="page-title mb-0">Liste des Planifications</h2>
            <div>
                <a href="${pageContext.request.contextPath}/pages/formulaire-planification" class="btn btn-primary me-2">Nouvelle Recherche</a>
                <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-secondary">Accueil</a>
            </div>
        </div>

        <div class="filter-section">
            <div class="row align-items-center">
                <div class="col-md-6">
                    <span class="date-display">📅 Date: <span id="selectedDate"></span></span>
                </div>
                <div class="col-md-6 text-end">
                    <span id="resultCount" class="text-muted"></span>
                </div>
            </div>
        </div>

        <div id="loadingDiv" class="loading">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
            </div>
            <p class="mt-2">Chargement des planifications...</p>
        </div>

        <div id="errorDiv" class="alert alert-danger" style="display: none;"></div>

        <div id="tableContainer" style="display: none;">
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

        <div id="emptyDiv" class="text-center py-5" style="display: none;">
            <h4 class="text-muted">Aucune planification trouvée pour cette date</h4>
            <p class="text-muted">Essayez une autre date ou créez de nouvelles planifications</p>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
    <script>
        const urlParams = new URLSearchParams(window.location.search);
        const date = urlParams.get('date') || new Date().toISOString().split('T')[0];
        
        document.getElementById('selectedDate').textContent = date;
        
        // Récupérer le token depuis localStorage ou sessionStorage
        const token = localStorage.getItem('api_token') || sessionStorage.getItem('api_token') || '';
        
        async function loadPlanifications() {
            const loadingDiv = document.getElementById('loadingDiv');
            const errorDiv = document.getElementById('errorDiv');
            const tableContainer = document.getElementById('tableContainer');
            const emptyDiv = document.getElementById('emptyDiv');
            const resultCount = document.getElementById('resultCount');
            
            loadingDiv.style.display = 'block';
            errorDiv.style.display = 'none';
            tableContainer.style.display = 'none';
            emptyDiv.style.display = 'none';
            
            try {
                const data = await fetchApi('${pageContext.request.contextPath}/planifications?date=' + date);
                loadingDiv.style.display = 'none';

                handleApiResponse(data, (planifications) => {
                    planifications = planifications || [];

                    if (planifications.length === 0) {
                        emptyDiv.style.display = 'block';
                        resultCount.textContent = '0 planification(s)';
                        return;
                    }

                    resultCount.textContent = planifications.length + ' planification(s)';

                    const tbody = document.getElementById('planificationTableBody');
                    tbody.innerHTML = '';

                    planifications.forEach(p => {
                        const depart = new Date(p.dateHeureDepartAeroport).toLocaleString('fr-FR');
                        const retour = new Date(p.dateHeureRetourAeroport).toLocaleString('fr-FR');

                        const row = document.createElement('tr');
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

                    tableContainer.style.display = 'block';
                }, (code, message) => {
                    errorDiv.textContent = message;
                    errorDiv.style.display = 'block';
                });

            } catch (error) {
                loadingDiv.style.display = 'none';
                errorDiv.textContent = 'Erreur de connexion: ' + error.message;
                errorDiv.style.display = 'block';
            }
        }
        
        // Charger les planifications au chargement de la page
        document.addEventListener('DOMContentLoaded', loadPlanifications);
    </script>
</body>
</html>
