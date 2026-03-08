<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resultat Planification Automatique</title>
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
            --warning-orange: #fd7e14;
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
            max-width: 1400px;
            margin: 0 auto;
            border: 1px solid var(--border-color);
        }
        .page-title {
            color: var(--dark);
            font-weight: 700;
            font-size: 1.5rem;
        }
        .filter-section {
            background: var(--light-bg);
            padding: 16px 20px;
            border-radius: 12px;
            margin-bottom: 24px;
            border: 1px solid var(--border-color);
        }
        .date-display {
            font-size: 1rem;
            color: var(--dark);
            font-weight: 600;
        }
        .date-display i {
            color: var(--primary-red);
            margin-right: 8px;
        }
        .table {
            margin-bottom: 0;
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
            padding: 12px;
            vertical-align: middle;
            font-size: 0.9rem;
            border-color: var(--border-color);
        }
        .table-warning-header thead {
            background: var(--warning-orange);
        }
        .loading {
            text-align: center;
            padding: 60px;
            color: var(--dark-secondary);
        }
        .loading i {
            color: var(--primary-red);
        }
        .section-title {
            color: var(--dark);
            font-weight: 700;
            font-size: 1.1rem;
            margin-top: 32px;
            margin-bottom: 16px;
            padding-bottom: 12px;
            border-bottom: 2px solid var(--dark);
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .section-title i {
            color: var(--success-green);
        }
        .section-title-warning i {
            color: var(--warning-orange);
        }
        .badge-count {
            font-size: 0.8rem;
            font-weight: 600;
            padding: 6px 12px;
            border-radius: 20px;
        }
        .badge-ordre {
            width: 28px;
            height: 28px;
            line-height: 28px;
            border-radius: 50%;
            display: inline-block;
            text-align: center;
            font-weight: 700;
            font-size: 0.85rem;
            color: white;
        }
        .badge-covoit {
            background: var(--dark);
            color: white;
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 600;
        }
        .btn-action {
            padding: 8px 16px;
            font-size: 0.875rem;
            border-radius: 8px;
        }
        .btn-primary-custom {
            background: var(--primary-red);
            border-color: var(--primary-red);
            color: white;
        }
        .btn-primary-custom:hover {
            background: var(--primary-red-dark);
            border-color: var(--primary-red-dark);
            color: white;
        }
        .btn-outline-custom {
            border-color: var(--border-color);
            color: var(--dark-secondary);
        }
        .btn-outline-custom:hover {
            background: var(--dark);
            border-color: var(--dark);
            color: white;
        }
        /* Itinerary cards */
        .itinerary-section-title {
            color: var(--dark);
            font-weight: 700;
            font-size: 1.1rem;
            margin-top: 40px;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .itinerary-section-title i {
            color: var(--primary-red);
        }
        .itinerary-card {
            border: 1px solid var(--border-color);
            border-radius: 12px;
            margin-bottom: 16px;
            overflow: hidden;
            background: white;
        }
        .itinerary-header {
            background: var(--dark);
            color: white;
            padding: 14px 20px;
            font-weight: 600;
            font-size: 0.95rem;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .itinerary-header i {
            color: var(--primary-red);
            background: white;
            width: 28px;
            height: 28px;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.85rem;
        }
        .itinerary-stop {
            display: flex;
            align-items: center;
            padding: 14px 20px;
            border-bottom: 1px solid var(--border-color);
        }
        .itinerary-stop:last-child {
            border-bottom: none;
        }
        .stop-number {
            background: var(--dark);
            color: white;
            width: 32px;
            height: 32px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 0.85rem;
            flex-shrink: 0;
            margin-right: 16px;
        }
        .stop-number.airport {
            background: var(--primary-red);
        }
        .stop-info {
            flex: 1;
        }
        .stop-info strong {
            color: var(--dark);
        }
        .stop-distance {
            color: #6c757d;
            font-size: 0.8rem;
            margin-top: 2px;
        }
        .stop-distance i {
            color: var(--primary-red);
            margin-right: 4px;
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
                <i class="fas fa-clipboard-check me-2" style="color: var(--primary-red);"></i>
                Resultat Planification
            </h2>
            <div>
                <a href="${pageContext.request.contextPath}/pages/formulaire-planification" class="btn btn-primary-custom btn-action me-2">
                    <i class="fas fa-redo me-1"></i> Nouvelle Recherche
                </a>
                <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-custom btn-action">
                    <i class="fas fa-home me-1"></i> Accueil
                </a>
            </div>
        </div>

        <div class="filter-section">
            <div class="row align-items-center">
                <div class="col-md-4">
                    <span class="date-display">
                        <i class="fas fa-calendar-alt"></i>
                        Date: <span id="selectedDate"></span>
                    </span>
                </div>
                <div class="col-md-8 text-end">
                    <span id="countAssigned" class="badge bg-success badge-count me-2"></span>
                    <span id="countNonAssigned" class="badge bg-warning text-dark badge-count"></span>
                </div>
            </div>
        </div>

        <div id="loadingDiv" class="loading">
            <i class="fas fa-spinner fa-spin fa-2x mb-3"></i>
            <p>Planification automatique en cours...</p>
        </div>

        <div id="errorDiv" class="alert alert-danger" style="display: none;"></div>

        <!-- Section Planifications Assignees -->
        <div id="planificationSection" style="display: none;">
            <h4 class="section-title">
                <i class="fas fa-check-circle"></i>
                Planifications Assignees
                <span id="planifCount" class="badge bg-success badge-count"></span>
            </h4>
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Reservation</th>
                            <th>Client</th>
                            <th>Hotel</th>
                            <th>Vehicule</th>
                            <th>Passagers</th>
                            <th>Covoiturage</th>
                            <th>Ordre</th>
                            <th>Depart</th>
                            <th>Retour</th>
                        </tr>
                    </thead>
                    <tbody id="planificationTableBody">
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Section Reservations Non Assignees -->
        <div id="nonAssignedSection" style="display: none;">
            <h4 class="section-title section-title-warning">
                <i class="fas fa-exclamation-triangle"></i>
                Reservations Non Assignees
                <span id="nonAssignedCount" class="badge bg-warning text-dark badge-count"></span>
            </h4>
            <p class="text-muted small mb-3">
                <i class="fas fa-info-circle me-1"></i>
                Aucun vehicule disponible n'a ete trouve pour ces reservations.
            </p>
            <div class="table-responsive">
                <table class="table table-hover table-warning-header">
                    <thead>
                        <tr>
                            <th>ID Reservation</th>
                            <th>Client</th>
                            <th>Passagers</th>
                            <th>Date/Heure Arrivee</th>
                            <th>Hotel</th>
                        </tr>
                    </thead>
                    <tbody id="nonAssignedTableBody">
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Section Itineraires -->
        <div id="itineraireSection" style="display: none;">
            <h4 class="itinerary-section-title">
                <i class="fas fa-map-marked-alt"></i>
                Itineraires de Depot par Vehicule
            </h4>
            <p class="text-muted small mb-3">
                Ordre de depot des clients : du lieu le plus proche de l'aeroport au plus eloigne.
            </p>
            <div id="itineraireContainer"></div>
        </div>

        <!-- Aucun resultat -->
        <div id="emptyDiv" class="empty-state" style="display: none;">
            <i class="fas fa-calendar-times"></i>
            <h5>Aucune reservation trouvee</h5>
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
            
            if (!token || token.trim() === '') {
                loadingDiv.style.display = 'none';
                errorDiv.innerHTML = '<i class="fas fa-exclamation-triangle me-2"></i>Token manquant. <a href="${pageContext.request.contextPath}/pages/gestion-tokens">Generez un token</a> puis rechargez la page.';
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
                loadingDiv.style.display = 'none';

                handleApiResponse(data, (result) => {
                    const planifications = result.planifications || [];
                    const reservationsNonAssignees = result.reservationsNonAssignees || [];

                    document.getElementById('countAssigned').textContent = planifications.length + ' assignee(s)';
                    document.getElementById('countNonAssigned').textContent = reservationsNonAssignees.length + ' non assignee(s)';

                    if (planifications.length === 0 && reservationsNonAssignees.length === 0) {
                        emptyDiv.style.display = 'block';
                        return;
                    }

                    if (planifications.length > 0) {
                        document.getElementById('planifCount').textContent = planifications.length;

                        const tbody = document.getElementById('planificationTableBody');
                        tbody.innerHTML = '';

                        planifications.forEach(function(p) {
                            var depart = new Date(p.dateHeureDepartAeroport).toLocaleString('fr-FR');
                            var retour = new Date(p.dateHeureRetourAeroport).toLocaleString('fr-FR');

                            var tripKey = (p.idVehicule || '') + '_' + p.dateHeureDepartAeroport + '_' + p.dateHeureRetourAeroport;
                            var copartageurs = planifications.filter(function(x) {
                                return (x.idVehicule || '') + '_' + x.dateHeureDepartAeroport + '_' + x.dateHeureRetourAeroport === tripKey;
                            });
                            var covBadge = copartageurs.length > 1
                                ? '<span class="badge-covoit"><i class="fas fa-users me-1"></i>' + copartageurs.length + ' clients</span>'
                                : '<span class="text-muted">-</span>';

                            var ordreColors = ['#198754','#0d6efd','#fd7e14','#c41e3a','#6f42c1'];
                            var ordreVal = p.ordreDepot || 1;
                            var ordreColor = ordreColors[(ordreVal - 1) % ordreColors.length];
                            var ordreBadge = '<span class="badge-ordre" style="background:' + ordreColor + ';">' + ordreVal + '</span>';

                            var row = document.createElement('tr');
                            row.innerHTML = '' +
                                '<td>' + escapeHtml(p.idPlanification) + '</td>' +
                                '<td>' + escapeHtml(p.idReservation) + '</td>' +
                                '<td><strong>' + escapeHtml(p.idClient || '-') + '</strong></td>' +
                                '<td>' + escapeHtml(p.nomHotel || '-') + '</td>' +
                                '<td><code>' + escapeHtml(p.referenceVehicule || p.idVehicule) + '</code></td>' +
                                '<td>' + escapeHtml(p.nbPassager || '-') + '</td>' +
                                '<td>' + covBadge + '</td>' +
                                '<td>' + ordreBadge + '</td>' +
                                '<td><small>' + escapeHtml(depart) + '</small></td>' +
                                '<td><small>' + escapeHtml(retour) + '</small></td>';
                            tbody.appendChild(row);
                        });

                        planificationSection.style.display = 'block';

                        // Build itineraries
                        var tripMap = {};
                        planifications.forEach(function(p) {
                            var key = (p.idVehicule || '') + '_' + p.dateHeureDepartAeroport + '_' + p.dateHeureRetourAeroport;
                            if (!tripMap[key]) tripMap[key] = { vehicule: p.referenceVehicule || ('Veh. ' + p.idVehicule), stops: [] };
                            tripMap[key].stops.push(p);
                        });

                        Object.values(tripMap).forEach(function(trip) {
                            trip.stops.sort(function(a, b) { return (a.ordreDepot || 1) - (b.ordreDepot || 1); });
                        });

                        var container = document.getElementById('itineraireContainer');
                        container.innerHTML = '';
                        var hasMultiStop = false;

                        Object.values(tripMap).forEach(function(trip) {
                            if (trip.stops.length < 1) return;
                            hasMultiStop = true;
                            var depart0 = new Date(trip.stops[0].dateHeureDepartAeroport).toLocaleString('fr-FR');
                            var html = '<div class="itinerary-card">';
                            html += '<div class="itinerary-header"><i class="fas fa-car"></i> ' + escapeHtml(trip.vehicule) +
                                    ' <span style="opacity:0.7;margin-left:auto;font-weight:400;font-size:0.85rem;">Depart: ' + escapeHtml(depart0) +
                                    ' | ' + trip.stops.length + ' arret(s)</span></div>';

                            html += '<div class="itinerary-stop" style="background:var(--light-bg);"><div class="stop-number airport"><i class="fas fa-plane"></i></div>' +
                                    '<div class="stop-info"><strong>Aeroport</strong> <span class="text-muted">(point de depart)</span></div></div>';

                            trip.stops.forEach(function(s) {
                                var dist = s.distanceAeroport >= 0 ? s.distanceAeroport.toFixed(1) + ' km' : 'distance inconnue';
                                var ordreColors2 = ['#198754','#0d6efd','#fd7e14','#c41e3a','#6f42c1'];
                                var oc = ordreColors2[((s.ordreDepot || 1) - 1) % ordreColors2.length];
                                html += '<div class="itinerary-stop">' +
                                    '<div class="stop-number" style="background:' + oc + '">' + (s.ordreDepot || 1) + '</div>' +
                                    '<div class="stop-info">' +
                                        '<strong>' + escapeHtml(s.nomHotel || '-') + '</strong>' +
                                        ' <span class="badge bg-secondary" style="font-size:0.7rem;">' + escapeHtml(s.idClient || '-') + '</span>' +
                                        ' <span class="badge bg-light text-dark" style="font-size:0.7rem;">' + escapeHtml(s.nbPassager) + ' pax</span>' +
                                        '<div class="stop-distance"><i class="fas fa-location-dot"></i>' + escapeHtml(dist) + ' de l\'aeroport</div>' +
                                    '</div>' +
                                '</div>';
                            });

                            html += '</div>';
                            container.innerHTML += html;
                        });

                        if (hasMultiStop) {
                            document.getElementById('itineraireSection').style.display = 'block';
                        }
                    }

                    if (reservationsNonAssignees.length > 0) {
                        document.getElementById('nonAssignedCount').textContent = reservationsNonAssignees.length;

                        var tbody2 = document.getElementById('nonAssignedTableBody');
                        tbody2.innerHTML = '';

                        reservationsNonAssignees.forEach(function(r) {
                            var dateArrivee = new Date(r.date_heure_arrivee).toLocaleString('fr-FR');

                            var row = document.createElement('tr');
                            row.innerHTML = '' +
                                '<td>' + escapeHtml(r.id_reservation) + '</td>' +
                                '<td><strong>' + escapeHtml(r.id_client || '-') + '</strong></td>' +
                                '<td>' + escapeHtml(r.nb_passager) + '</td>' +
                                '<td><small>' + escapeHtml(dateArrivee) + '</small></td>' +
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
