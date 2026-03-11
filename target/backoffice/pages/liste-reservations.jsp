<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des Reservations</title>
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
        .filter-section {
            background: var(--light-bg);
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 24px;
            border: 1px solid var(--border-color);
        }
        .filter-section .form-label {
            font-weight: 600;
            color: var(--dark);
            font-size: 0.9rem;
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
        .badge-id {
            background: var(--primary-red);
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 600;
        }
        .loading {
            text-align: center;
            padding: 60px;
            color: var(--dark-secondary);
        }
        .loading i {
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
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: var(--dark-secondary);
        }
        .empty-state i {
            font-size: 3rem;
            color: var(--border-color);
            margin-bottom: 16px;
        }
    </style>
</head>
<body>
    <div class="container-custom">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="page-title mb-0">
                <i class="fas fa-list-alt"></i>
                Liste des Reservations
            </h2>
            <div>
                <a href="${pageContext.request.contextPath}/pages/formulaire-reservation" class="btn btn-primary-custom me-2">
                    <i class="fas fa-plus me-2"></i>Nouvelle Reservation
                </a>
                <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-custom">
                    <i class="fas fa-home me-2"></i>Accueil
                </a>
            </div>
        </div>

        <div class="filter-section">
            <div class="row align-items-end">
                <div class="col-md-4">
                    <label for="filterDate" class="form-label">
                        <i class="fas fa-filter me-2" style="color: var(--primary-red);"></i>Filtrer par date
                    </label>
                    <input type="date" class="form-control" id="filterDate">
                </div>
                <div class="col-md-2">
                    <button class="btn btn-primary-custom w-100" onclick="filterByDate()">
                        <i class="fas fa-search me-2"></i>Filtrer
                    </button>
                </div>
                <div class="col-md-2">
                    <button class="btn btn-outline-custom w-100" onclick="showAll()">
                        <i class="fas fa-list me-2"></i>Tout
                    </button>
                </div>
            </div>
        </div>

        <div id="loadingDiv" class="loading">
            <i class="fas fa-spinner fa-spin fa-2x mb-3"></i>
            <p>Chargement des reservations...</p>
        </div>

        <div id="tableContainer" style="display: none;">
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th><i class="fas fa-hashtag me-2"></i>ID Reservation</th>
                            <th><i class="fas fa-user me-2"></i>ID Client</th>
                            <th><i class="fas fa-hotel me-2"></i>Hotel</th>
                            <th><i class="fas fa-users me-2"></i>Passagers</th>
                            <th><i class="fas fa-calendar me-2"></i>Date et heure d'arrivee</th>
                        </tr>
                    </thead>
                    <tbody id="reservationTableBody">
                    </tbody>
                </table>
            </div>
            <div id="emptyMessage" class="empty-state" style="display: none;">
                <i class="fas fa-calendar-times"></i>
                <h5>Aucune reservation trouvee</h5>
                <p class="text-muted">Creez une nouvelle reservation pour commencer</p>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';

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
                    '<td><span class="badge-id">' + escapeHtml(reservation.id_reservation) + '</span></td>' +
                    '<td><strong>' + escapeHtml(reservation.id_client) + '</strong></td>' +
                    '<td>' + escapeHtml(reservation.nom_hotel || 'N/A') + '</td>' +
                    '<td>' + escapeHtml(reservation.nb_passager) + ' <i class="fas fa-user text-muted" style="font-size: 0.8rem;"></i></td>' +
                    '<td><i class="fas fa-clock text-muted me-2" style="font-size: 0.8rem;"></i>' + escapeHtml(formattedDate) + '</td>';

                tbody.appendChild(row);
            });

            document.getElementById('emptyMessage').style.display = 'none';
        }

        function filterByDate() {
            const dateInput = document.getElementById('filterDate');
            if (dateInput.value) {
                loadReservations(contextPath + '/reservations?date=' + dateInput.value);
            } else {
                alert('Veuillez selectionner une date');
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
