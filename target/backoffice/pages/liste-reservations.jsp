<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="fr">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Liste des Réservations</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
            body {
                /* background: linear-gradient(135deg, #17245f 0%, #0b0242 100%); */
                background: linear-gradient(135deg, #5a5a5a 0%, #111111 100%);
                min-height: 100vh;
                padding: 40px 20px;
            }

            .container-custom {
                background: white;
                border-radius: 15px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
                padding: 40px;
                max-width: 1200px;
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
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
            }

            .table thead {
                background: #17245f;
                color: white;
            }

            .badge-custom {
                padding: 8px 12px;
                font-weight: 500;
            }

            .loading {
                text-align: center;
                padding: 40px;
                color: #17245f;
            }
        </style>
    </head>

    <body>
        <div class="container-custom">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="page-title mb-0">Liste des Réservations</h2>
                <div>
                    <a href="${pageContext.request.contextPath}/pages/formulaire-reservation"
                        class="btn btn-primary me-2">Nouvelle Réservation</a>
                    <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-secondary">Accueil</a>
                </div>
            </div>

            <div class="filter-section">
                <div class="row align-items-end">
                    <div class="col-md-4">
                        <label for="filterDate" class="form-label fw-bold">Filtrer par date</label>
                        <input type="date" class="form-control" id="filterDate">
                    </div>
                    <div class="col-md-2">
                        <button class="btn btn-primary w-100" onclick="filterByDate()">Filtrer</button>
                    </div>
                    <div class="col-md-2">
                        <button class="btn btn-outline-secondary w-100" onclick="showAll()">Afficher tout</button>
                    </div>
                </div>
            </div>

            <div id="loadingDiv" class="loading">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Chargement...</span>
                </div>
                <p class="mt-2">Chargement des réservations...</p>
            </div>

            <div id="tableContainer" style="display: none;">
                <div class="table-responsive">
                    <table class="table table-hover table-custom">
                        <thead>
                            <tr>
                                <th>ID Réservation</th>
                                <th>ID Client</th>
                                <th>Hôtel</th>
                                <th>Nombre de passagers</th>
                                <th>Date et heure d'arrivée</th>
                            </tr>
                        </thead>
                        <tbody id="reservationTableBody">
                        </tbody>
                    </table>
                </div>
                <div id="emptyMessage" class="text-center text-muted py-5" style="display: none;">
                    <h5>Aucune réservation trouvée</h5>
                    <p>Créez une nouvelle réservation pour commencer</p>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
        <script>
            const contextPath = '${pageContext.request.contextPath}';

            // ===== CHARGEMENT DES RÉSERVATIONS =====
            async function loadReservations(url = contextPath + '/reservations') {
                document.getElementById('loadingDiv').style.display = 'block';
                document.getElementById('tableContainer').style.display = 'none';

                try {
                    const result = await fetchApi(url);

                    handleApiResponse(result, (data) => {
                        displayReservations(data);
                        document.getElementById('loadingDiv').style.display = 'none';
                        document.getElementById('tableContainer').style.display = 'block';
                    }, (code, message) => {
                        if (code === 403) {
                            // Réinitialiser le token et redemander
                            clearStoredToken();
                            loadReservations(url);
                        }
                    });
                } catch (error) {
                    console.error('Erreur:', error);
                    showApiError(0, 'Erreur lors du chargement: ' + error.message);
                    document.getElementById('loadingDiv').style.display = 'none';
                }
            }

            function displayReservations(reservations) {
                const tbody = document.getElementById('reservationTableBody');
                tbody.innerHTML = '';

                if (!Array.isArray(reservations) || reservations.length === 0) {
                    document.getElementById('emptyMessage').style.display = 'block';
                    return;
                }

                reservations.forEach((reservation) => {
                    const date = new Date(reservation.date_heure_arrivee);
                    const formattedDate = date.toLocaleString('fr-FR', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit'
                    });

                    const row = document.createElement('tr');
                    row.innerHTML = '' +
                        '<td><span class="badge bg-primary badge-custom">' + escapeHtml(reservation.id_reservation) + '</span></td>' +
                        '<td><strong>' + escapeHtml(reservation.id_client) + '</strong></td>' +
                        '<td>' + escapeHtml(reservation.nom_hotel || 'N/A') + '</td>' +
                        '<td>' + escapeHtml(reservation.nb_passager) + '</td>' +
                        '<td>' + escapeHtml(formattedDate) + '</td>';

                    tbody.appendChild(row);
                });

                document.getElementById('emptyMessage').style.display = 'none';
            }

            function filterByDate() {
                const dateInput = document.getElementById('filterDate');
                if (dateInput.value) {
                    loadReservations(contextPath + '/reservations?date=' + dateInput.value);
                } else {
                    alert('Veuillez sélectionner une date');
                }
            }

            function showAll() {
                document.getElementById('filterDate').value = '';
                loadReservations();
            }

            window.onload = function () {
                loadReservations();
            };
        </script>
    </body>

    </html>