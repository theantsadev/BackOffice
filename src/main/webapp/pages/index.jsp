<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BackOffice - Gestion des Réservations</title>
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
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
        }
        .main-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.08);
            padding: 48px;
            max-width: 520px;
            width: 100%;
            border: 1px solid var(--border-color);
        }
        .main-title {
            color: var(--dark);
            margin-bottom: 8px;
            font-weight: 700;
            font-size: 1.75rem;
        }
        .subtitle {
            color: #6c757d;
            margin-bottom: 32px;
            font-size: 0.95rem;
        }
        .section-label {
            color: var(--dark-secondary);
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-top: 24px;
            margin-bottom: 12px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .section-label i {
            color: var(--primary-red);
            font-size: 0.85rem;
        }
        .menu-btn {
            margin: 6px 0;
            padding: 14px 20px;
            font-size: 0.95rem;
            font-weight: 500;
            border-radius: 10px;
            display: flex;
            align-items: center;
            gap: 12px;
            transition: all 0.2s ease;
        }
        .menu-btn i {
            width: 20px;
            text-align: center;
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
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(196, 30, 58, 0.3);
        }
        .btn-outline-custom {
            border-color: var(--border-color);
            color: var(--dark-secondary);
        }
        .btn-outline-custom:hover {
            background: var(--dark);
            border-color: var(--dark);
            color: white;
            transform: translateY(-1px);
        }
        .logo-icon {
            width: 48px;
            height: 48px;
            background: var(--primary-red);
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 20px;
        }
        .logo-icon i {
            color: white;
            font-size: 1.5rem;
        }
    </style>
</head>
<body>
    <div class="main-card">
        <div class="logo-icon">
            <i class="fas fa-building"></i>
        </div>
        <h1 class="main-title">Gestion BackOffice</h1>
        <p class="subtitle">Systeme de gestion des transports hoteliers</p>
        
        <div class="d-grid gap-2">
            <div class="section-label">
                <i class="fas fa-calendar-check"></i>
                Reservations
            </div>
            <a href="${pageContext.request.contextPath}/pages/formulaire-reservation" class="btn btn-primary-custom menu-btn">
                <i class="fas fa-plus"></i>
                Nouvelle Reservation
            </a>
            <a href="${pageContext.request.contextPath}/pages/liste-reservations" class="btn btn-outline-custom menu-btn">
                <i class="fas fa-list"></i>
                Voir les Reservations
            </a>
            
            <div class="section-label">
                <i class="fas fa-car"></i>
                Vehicules
            </div>
            <a href="${pageContext.request.contextPath}/pages/formulaire-vehicule" class="btn btn-primary-custom menu-btn">
                <i class="fas fa-plus"></i>
                Nouveau Vehicule
            </a>
            <a href="${pageContext.request.contextPath}/pages/liste-vehicules" class="btn btn-outline-custom menu-btn">
                <i class="fas fa-list"></i>
                Voir les Vehicules
            </a>
            
            <div class="section-label">
                <i class="fas fa-route"></i>
                Planifications
            </div>
            <a href="${pageContext.request.contextPath}/pages/formulaire-planification" class="btn btn-primary-custom menu-btn">
                <i class="fas fa-search"></i>
                Rechercher Planifications
            </a>
            
            <div class="section-label">
                <i class="fas fa-key"></i>
                Securite
            </div>
            <a href="${pageContext.request.contextPath}/pages/gestion-tokens" class="btn btn-outline-custom menu-btn">
                <i class="fas fa-shield-halved"></i>
                Gestion des Tokens
            </a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
