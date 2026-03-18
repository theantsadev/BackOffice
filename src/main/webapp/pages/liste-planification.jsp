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
        .rules-box {
            background: #fff7e6;
            border: 1px solid #ffd89c;
            border-radius: 12px;
            padding: 16px 18px;
            margin-bottom: 18px;
        }
        .rules-title {
            font-weight: 700;
            color: #7a4b00;
            margin-bottom: 8px;
        }
        .rules-line {
            font-size: 0.88rem;
            color: #6a4a1d;
            margin-bottom: 4px;
        }
        .interactive-toolbar {
            background: #ffffff;
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 14px;
            margin-bottom: 20px;
        }
        .group-card {
            border: 1px solid var(--border-color);
            border-radius: 10px;
            padding: 12px;
            margin-bottom: 10px;
            background: #fff;
        }
        .group-title {
            font-weight: 700;
            color: var(--dark);
            font-size: 0.9rem;
        }
        .group-meta {
            color: #6c757d;
            font-size: 0.82rem;
        }
        .compact-pill {
            display: inline-block;
            border-radius: 999px;
            padding: 2px 9px;
            font-size: 0.75rem;
            font-weight: 600;
            background: #eef1f4;
            color: #334;
            margin-right: 5px;
        }
        .trajet-list {
            margin: 0;
            padding-left: 18px;
        }
        .trajet-list li {
            font-size: 0.82rem;
            margin-bottom: 2px;
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

        <div class="rules-box">
            <div class="rules-title"><i class="fas fa-scale-balanced me-2"></i>Regles Sprint 5 appliquees</div>
            <div class="rules-line">1. Fenetre d'attente: <span id="waitMinutesLabel">-</span> min (parametrable), groupe = reservations dans la meme fenetre.</div>
            <div class="rules-line">2. Groupe de reservation: meme date de depart (depart = arrivee la plus recente du groupe).</div>
            <div class="rules-line">3. Priorite d'assignation: plus grand nombre de passagers d'abord.</div>
            <div class="rules-line">4. Itineraire: Aeroport -> hotels (sans doublon) -> Aeroport, avec distances detaillees.</div>
        </div>

        <div class="interactive-toolbar">
            <div class="row g-2 align-items-center">
                <div class="col-md-5">
                    <input id="searchInput" type="text" class="form-control" placeholder="Filtrer par client, hotel, vehicule...">
                </div>
                <div class="col-md-3">
                    <select id="groupFilter" class="form-select">
                        <option value="all">Tous les groupes de depart</option>
                    </select>
                </div>
                <div class="col-md-4 text-md-end">
                    <span id="kpiGroupCount" class="compact-pill">0 groupe</span>
                    <span id="kpiVehiculeCount" class="compact-pill">0 vehicule</span>
                    <span id="kpiPaxCount" class="compact-pill">0 pax</span>
                </div>
            </div>
        </div>

        <div id="groupSection" style="display:none; margin-bottom: 18px;">
            <h4 class="section-title" style="margin-top:0;">
                <i class="fas fa-layer-group"></i>
                Lecture Rapide Par Groupe De Depart
            </h4>
            <div id="groupContainer"></div>
        </div>

        <div id="trajetsSection" style="display:none; margin-bottom: 18px;">
            <h4 class="section-title" style="margin-top:0;">
                <i class="fas fa-route"></i>
                Synthese Des Trajets (Sprint 5)
            </h4>
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>Heure Depart</th>
                            <th>Vehicule</th>
                            <th>Liste Reservations</th>
                            <th>Distance Parcourue</th>
                            <th>Heure Retour</th>
                        </tr>
                    </thead>
                    <tbody id="trajetsTableBody"></tbody>
                </table>
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
                            <th>Ordre Assign Groupe</th>
                            <th>Ordre Assign Global</th>
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
                            <th>Depart Estime Groupe</th>
                            <th>Ordre Assign Groupe</th>
                            <th>Ordre Assign Global</th>
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
        let allDepartGroupsCache = [];

        function formatFr(dateLike) {
            return new Date(dateLike).toLocaleString('fr-FR');
        }

        function toDepartKey(dateLike) {
            const t = new Date(dateLike).getTime();
            return Number.isNaN(t) ? '' : String(t);
        }

        function buildTripMap(planifications) {
            const tripMap = {};
            planifications.forEach(function(p) {
                const key = (p.idVehicule || '') + '_' + p.dateHeureDepartAeroport + '_' + p.dateHeureRetourAeroport;
                if (!tripMap[key]) {
                    tripMap[key] = {
                        key: key,
                        vehicule: p.referenceVehicule || ('Veh. ' + p.idVehicule),
                        depart: p.dateHeureDepartAeroport,
                        retour: p.dateHeureRetourAeroport,
                        stops: [],
                        totalPax: 0
                    };
                }
                tripMap[key].stops.push(p);
                tripMap[key].totalPax += Number(p.nbPassager || 0);
            });

            Object.values(tripMap).forEach(function(trip) {
                trip.stops.sort(function(a, b) { return (a.ordreDepot || 1) - (b.ordreDepot || 1); });
            });
            return tripMap;
        }

        function buildDepartGroups(planifications, reservationsNonAssignees) {
            const groups = {};

            planifications.forEach(function(p) {
                const key = toDepartKey(p.dateHeureDepartAeroport);
                if (!key) return;
                if (!groups[key]) {
                    groups[key] = {
                        key: key,
                        depart: p.dateHeureDepartAeroport,
                        assignedCount: 0,
                        nonAssignedCount: 0,
                        paxAssigned: 0,
                        paxNonAssigned: 0
                    };
                }
                groups[key].assignedCount += 1;
                groups[key].paxAssigned += Number(p.nbPassager || 0);
            });

            reservationsNonAssignees.forEach(function(r) {
                if (!r.date_heure_depart_groupe) return;
                const key = toDepartKey(r.date_heure_depart_groupe);
                if (!key) return;
                if (!groups[key]) {
                    groups[key] = {
                        key: key,
                        depart: r.date_heure_depart_groupe,
                        assignedCount: 0,
                        nonAssignedCount: 0,
                        paxAssigned: 0,
                        paxNonAssigned: 0
                    };
                }
                groups[key].nonAssignedCount += 1;
                groups[key].paxNonAssigned += Number(r.nb_passager || 0);
            });

            return Object.values(groups).sort(function(a, b) {
                return new Date(a.depart).getTime() - new Date(b.depart).getTime();
            });
        }

        
        
        async function loadPlanifications() {
            const loadingDiv = document.getElementById('loadingDiv');
            const errorDiv = document.getElementById('errorDiv');
            const planificationSection = document.getElementById('planificationSection');
            const nonAssignedSection = document.getElementById('nonAssignedSection');
            const emptyDiv = document.getElementById('emptyDiv');
            const groupSection = document.getElementById('groupSection');
            const groupContainer = document.getElementById('groupContainer');
            const trajetsSection = document.getElementById('trajetsSection');
            const trajetsTableBody = document.getElementById('trajetsTableBody');
            const searchInput = document.getElementById('searchInput');
            const groupFilter = document.getElementById('groupFilter');
            
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
            groupSection.style.display = 'none';
            trajetsSection.style.display = 'none';
            
            try {
                const selectedGroupValue = groupFilter.value || 'all';
                const departGroupParam = selectedGroupValue !== 'all'
                    ? '&depart_groupe=' + encodeURIComponent(selectedGroupValue)
                    : '&depart_groupe=';
                const data = await fetchApi('${pageContext.request.contextPath}/planifications?date=' + date + departGroupParam);
                loadingDiv.style.display = 'none';

                handleApiResponse(data, (result) => {
                    const planifications = result.planifications || [];
                    const reservationsNonAssigneesRaw = result.reservationsNonAssignees || [];
                    const reservationsNonAssignees = reservationsNonAssigneesRaw;
                    const waitMinutes = Number(result.tempsAttenteMin || 0);
                    document.getElementById('waitMinutesLabel').textContent = waitMinutes > 0 ? String(waitMinutes) : '-';
                    const tripMap = buildTripMap(planifications);
                    const tripList = Object.values(tripMap).sort(function(a, b) {
                        return new Date(a.depart).getTime() - new Date(b.depart).getTime();
                    });
                    const departGroups = buildDepartGroups(planifications, reservationsNonAssignees);
                    if (selectedGroupValue === 'all' || allDepartGroupsCache.length === 0) {
                        allDepartGroupsCache = departGroups.slice();
                    }
                    const groupsForFilter = allDepartGroupsCache;
                    const q = (searchInput.value || '').trim().toLowerCase();
                    const selectedBefore = selectedGroupValue;

                    const distinctVehicules = new Set(planifications.map(function(p) { return p.idVehicule; })).size;
                    const totalPax = planifications.reduce(function(acc, p) { return acc + Number(p.nbPassager || 0); }, 0);
                    document.getElementById('kpiGroupCount').textContent = groupsForFilter.length + ' groupe(s)';
                    document.getElementById('kpiVehiculeCount').textContent = distinctVehicules + ' vehicule(s)';
                    document.getElementById('kpiPaxCount').textContent = totalPax + ' pax';

                    groupFilter.innerHTML = '<option value="all">Tous les groupes de depart</option>';
                    groupsForFilter.forEach(function(g) {
                        const opt = document.createElement('option');
                        opt.value = g.key;
                        opt.textContent = formatFr(g.depart) + ' | ' + (g.assignedCount + g.nonAssignedCount) + ' reservation(s)';
                        groupFilter.appendChild(opt);
                    });
                    if (selectedBefore !== 'all') {
                        const hasSelected = groupsForFilter.some(function(g) { return g.key === selectedBefore; });
                        groupFilter.value = hasSelected ? selectedBefore : 'all';
                    }
                    const selectedGroup = groupFilter.value || 'all';

                    groupContainer.innerHTML = '';
                    trajetsTableBody.innerHTML = '';
                    departGroups.forEach(function(g) {
                        if (selectedGroup !== 'all' && g.key !== selectedGroup) return;
                        const card = document.createElement('div');
                        card.className = 'group-card';
                        card.innerHTML = '' +
                            '<div class="group-title">Depart groupe: ' + escapeHtml(formatFr(g.depart)) + '</div>' +
                            '<div class="group-meta">Reservations assignees: <strong>' + g.assignedCount + '</strong> | Non assignees: <strong>' + g.nonAssignedCount + '</strong> | Pax assigne: ' + g.paxAssigned + ' | Pax non assigne: ' + g.paxNonAssigned + '</div>';
                        groupContainer.appendChild(card);
                    });

                    tripList.forEach(function(t) {
                        if (selectedGroup !== 'all' && toDepartKey(t.depart) !== selectedGroup) return;
                        if (q) {
                            const match = (t.vehicule + ' ' + t.stops.map(function(s) {
                                return String(s.idReservation || '') + ' ' + (s.idClient || '') + ' ' + (s.nomHotel || '');
                            }).join(' ')).toLowerCase();
                            if (match.indexOf(q) < 0) return;
                        }

                        const uniqueByReservation = {};
                        t.stops.forEach(function(s) {
                            uniqueByReservation[String(s.idReservation)] = s;
                        });
                        const reservationsListe = Object.values(uniqueByReservation).sort(function(a, b) {
                            return Number(a.ordreDepot || 0) - Number(b.ordreDepot || 0);
                        });
                        const reservationHtml = '<ul class="trajet-list">' + reservationsListe.map(function(s) {
                            return '<li>#' + escapeHtml(s.idReservation) +
                                ' - ' + escapeHtml(s.idClient || '-') +
                                ' (' + escapeHtml(s.nbPassager || '-') + ' pax, ' +
                                escapeHtml(s.nomHotel || '-') + ')</li>';
                        }).join('') + '</ul>';

                        const distanceTrajet = typeof t.stops[0].distanceTotaleTrajet === 'number'
                            ? t.stops[0].distanceTotaleTrajet.toFixed(1) + ' km'
                            : '-';

                        const trajetRow = document.createElement('tr');
                        trajetRow.innerHTML = '' +
                            '<td><small>' + escapeHtml(formatFr(t.depart)) + '</small></td>' +
                            '<td><strong>' + escapeHtml(t.vehicule) + '</strong></td>' +
                            '<td>' + reservationHtml + '</td>' +
                            '<td><span class="badge bg-dark">' + escapeHtml(distanceTrajet) + '</span></td>' +
                            '<td><small>' + escapeHtml(formatFr(t.retour)) + '</small></td>';
                        trajetsTableBody.appendChild(trajetRow);
                    });
                    groupSection.style.display = groupContainer.children.length > 0 ? 'block' : 'none';
                    trajetsSection.style.display = trajetsTableBody.children.length > 0 ? 'block' : 'none';

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
                            if (selectedGroup !== 'all' && toDepartKey(p.dateHeureDepartAeroport) !== selectedGroup) return;
                            if (q) {
                                const hay = [p.idClient, p.nomHotel, p.referenceVehicule, p.idReservation, p.idPlanification]
                                    .map(function(v) { return String(v || '').toLowerCase(); }).join(' ');
                                if (hay.indexOf(q) < 0) return;
                            }

                            var depart = new Date(p.dateHeureDepartAeroport).toLocaleString('fr-FR');
                            var retour = new Date(p.dateHeureRetourAeroport).toLocaleString('fr-FR');

                            var tripKey = (p.idVehicule || '') + '_' + p.dateHeureDepartAeroport + '_' + p.dateHeureRetourAeroport;
                            var copartageurs = tripMap[tripKey] ? tripMap[tripKey].stops : [];
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
                                '<td><span class="badge bg-primary">#' + escapeHtml(p.ordreAssignGroupe || '-') + '</span></td>' +
                                '<td><span class="badge bg-dark">#' + escapeHtml(p.ordreAssignGlobal || '-') + '</span></td>' +
                                '<td><small>' + escapeHtml(depart) + '</small></td>' +
                                '<td><small>' + escapeHtml(retour) + '</small></td>';
                            tbody.appendChild(row);
                        });

                        planificationSection.style.display = 'block';

                        var container = document.getElementById('itineraireContainer');
                        container.innerHTML = '';
                        var hasItinerary = false;

                        Object.values(tripMap).forEach(function(trip) {
                            if (selectedGroup !== 'all' && toDepartKey(trip.depart) !== selectedGroup) return;
                            if (q) {
                                var tripHay = (trip.vehicule + ' ' + trip.stops.map(function(s) {
                                    return (s.nomHotel || '') + ' ' + (s.idClient || '');
                                }).join(' ')).toLowerCase();
                                if (tripHay.indexOf(q) < 0) return;
                            }

                            if (trip.stops.length < 1) return;
                            hasItinerary = true;

                            var uniqueStops = [];
                            var stopIndexByHotel = {};
                            trip.stops.forEach(function(s) {
                                var hotelKey = String(s.idHotel || s.nomHotel || '-');
                                if (stopIndexByHotel[hotelKey] === undefined) {
                                    stopIndexByHotel[hotelKey] = uniqueStops.length;
                                    uniqueStops.push({
                                        nomHotel: s.nomHotel || '-',
                                        distanceAeroport: (typeof s.distanceAeroport === 'number') ? s.distanceAeroport : -1,
                                        distanceSegmentKm: (typeof s.distanceSegmentKm === 'number') ? s.distanceSegmentKm : -1,
                                        distanceProgressiveKm: (typeof s.distanceProgressiveKm === 'number') ? s.distanceProgressiveKm : -1,
                                        nbPassager: Number(s.nbPassager || 0),
                                        clients: (s.idClient ? [s.idClient] : [])
                                    });
                                } else {
                                    var idxStop = stopIndexByHotel[hotelKey];
                                    uniqueStops[idxStop].nbPassager += Number(s.nbPassager || 0);
                                    if (s.idClient) {
                                        uniqueStops[idxStop].clients.push(s.idClient);
                                    }
                                }
                            });

                            var routeNodes = ['Aeroport'];
                            uniqueStops.forEach(function(stop) {
                                routeNodes.push(stop.nomHotel || '-');
                            });
                            routeNodes.push('Aeroport');
                            var routeLabel = routeNodes.join(' -> ');

                            var depart0 = new Date(trip.stops[0].dateHeureDepartAeroport).toLocaleString('fr-FR');
                            var distanceTotale = typeof trip.stops[0].distanceTotaleTrajet === 'number'
                                ? trip.stops[0].distanceTotaleTrajet.toFixed(1) + ' km'
                                : '-';

                            var html = '<div class="itinerary-card">';
                            html += '<div class="itinerary-header"><i class="fas fa-car"></i> ' + escapeHtml(trip.vehicule) +
                                    ' <span style="opacity:0.7;margin-left:auto;font-weight:400;font-size:0.85rem;">Depart: ' + escapeHtml(depart0) +
                                    ' | ' + uniqueStops.length + ' arret(s)' +
                                    ' | Distance totale: ' + escapeHtml(distanceTotale) + '</span></div>';

                            html += '<div class="itinerary-stop" style="background:#f1f3f5;">' +
                                    '<div class="stop-number airport"><i class="fas fa-route"></i></div>' +
                                    '<div class="stop-info"><strong>Trajet complet</strong>' +
                                    '<div class="stop-distance"><i class="fas fa-arrow-right"></i>' + escapeHtml(routeLabel) + '</div>' +
                                    '</div></div>';

                            html += '<div class="itinerary-stop" style="background:var(--light-bg);"><div class="stop-number airport"><i class="fas fa-plane"></i></div>' +
                                    '<div class="stop-info"><strong>Aeroport</strong> <span class="text-muted">(point de depart)</span></div></div>';

                            uniqueStops.forEach(function(s, indexStop) {
                                var distVal = (typeof s.distanceAeroport === 'number' && s.distanceAeroport >= 0) ? s.distanceAeroport : -1;
                                var dist = distVal >= 0 ? distVal.toFixed(1) + ' km' : 'distance inconnue';
                                var segmentVal = (typeof s.distanceSegmentKm === 'number' && s.distanceSegmentKm >= 0)
                                    ? s.distanceSegmentKm
                                    : -1;
                                var progressifVal = (typeof s.distanceProgressiveKm === 'number' && s.distanceProgressiveKm >= 0)
                                    ? s.distanceProgressiveKm
                                    : -1;
                                var segmentLabel = segmentVal >= 0 ? segmentVal.toFixed(1) + ' km' : 'non calculee';
                                var progressionLabel = progressifVal >= 0 ? progressifVal.toFixed(1) + ' km' : 'non calculee';
                                var ordreColors2 = ['#198754','#0d6efd','#fd7e14','#c41e3a','#6f42c1'];
                                var oc = ordreColors2[(indexStop) % ordreColors2.length];
                                var clientsLabel = s.clients.length > 0 ? s.clients.join(', ') : '-';
                                html += '<div class="itinerary-stop">' +
                                    '<div class="stop-number" style="background:' + oc + '">' + (indexStop + 1) + '</div>' +
                                    '<div class="stop-info">' +
                                        '<strong>' + escapeHtml(s.nomHotel || '-') + '</strong>' +
                                        ' <span class="badge bg-secondary" style="font-size:0.7rem;">' + escapeHtml(clientsLabel) + '</span>' +
                                        ' <span class="badge bg-light text-dark" style="font-size:0.7rem;">' + escapeHtml(s.nbPassager) + ' pax</span>' +
                                        '<div class="stop-distance"><i class="fas fa-location-dot"></i>' + escapeHtml(dist) + ' de l\'aeroport</div>' +
                                        '<div class="stop-distance"><i class="fas fa-ruler-horizontal"></i>Segment precedent: ' + escapeHtml(segmentLabel) + '</div>' +
                                        '<div class="stop-distance"><i class="fas fa-road"></i>Progressif: ' + escapeHtml(progressionLabel) + '</div>' +
                                    '</div>' +
                                '</div>';
                            });

                            html += '<div class="itinerary-stop" style="background:var(--light-bg);">' +
                                    '<div class="stop-number airport"><i class="fas fa-plane-arrival"></i></div>' +
                                    '<div class="stop-info"><strong>Aeroport</strong> <span class="text-muted">(retour)</span>' +
                                    '<div class="stop-distance"><i class="fas fa-road"></i>Distance totale du trajet: ' + escapeHtml(distanceTotale) + '</div>' +
                                    '</div></div>';

                            html += '</div>';
                            container.innerHTML += html;
                        });

                        if (hasItinerary) {
                            document.getElementById('itineraireSection').style.display = 'block';
                        }
                    }

                    if (reservationsNonAssignees.length > 0) {
                        document.getElementById('nonAssignedCount').textContent = reservationsNonAssignees.length;

                        var tbody2 = document.getElementById('nonAssignedTableBody');
                        tbody2.innerHTML = '';

                        reservationsNonAssignees.forEach(function(r) {
                            if (selectedGroup !== 'all' && toDepartKey(r.date_heure_depart_groupe) !== selectedGroup) return;
                            if (q) {
                                var nonHay = [r.id_client, r.nom_hotel, r.id_reservation].map(function(v) {
                                    return String(v || '').toLowerCase();
                                }).join(' ');
                                if (nonHay.indexOf(q) < 0) return;
                            }
                            var dateArrivee = new Date(r.date_heure_arrivee).toLocaleString('fr-FR');
                            var departEstime = r.date_heure_depart_groupe ? formatFr(r.date_heure_depart_groupe) : '-';

                            var row = document.createElement('tr');
                            row.innerHTML = '' +
                                '<td>' + escapeHtml(r.id_reservation) + '</td>' +
                                '<td><strong>' + escapeHtml(r.id_client || '-') + '</strong></td>' +
                                '<td>' + escapeHtml(r.nb_passager) + '</td>' +
                                '<td><small>' + escapeHtml(dateArrivee) + '</small></td>' +
                                '<td>' + escapeHtml(r.nom_hotel || '-') + '</td>' +
                                '<td><small>' + escapeHtml(departEstime) + '</small></td>' +
                                '<td><span class="badge bg-primary">#' + escapeHtml(r.ordre_assign_groupe || '-') + '</span></td>' +
                                '<td><span class="badge bg-dark">#' + escapeHtml(r.ordre_assign_global || '-') + '</span></td>';
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

        document.getElementById('searchInput').addEventListener('input', loadPlanifications);
        document.getElementById('groupFilter').addEventListener('change', loadPlanifications);
        
        document.addEventListener('DOMContentLoaded', loadPlanifications);
    </script>
</body>
</html>
