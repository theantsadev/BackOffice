<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recherche Planifications</title>
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
            padding: 40px 20px;
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
        }
        .form-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.08);
            padding: 40px;
            max-width: 500px;
            margin: 0 auto;
            border: 1px solid var(--border-color);
        }
        .form-title {
            color: var(--dark);
            margin-bottom: 8px;
            font-weight: 700;
            font-size: 1.5rem;
        }
        .form-subtitle {
            color: #6c757d;
            margin-bottom: 32px;
            font-size: 0.9rem;
        }
        .form-label {
            font-weight: 600;
            color: var(--dark-secondary);
            font-size: 0.875rem;
        }
        .form-control {
            border-radius: 10px;
            border: 1px solid var(--border-color);
            padding: 12px 16px;
            font-size: 0.95rem;
        }
        .form-control:focus {
            border-color: var(--primary-red);
            box-shadow: 0 0 0 3px rgba(196, 30, 58, 0.1);
        }
        .btn-submit {
            background: var(--primary-red);
            border-color: var(--primary-red);
            color: white;
            padding: 14px 40px;
            font-weight: 600;
            border-radius: 10px;
            font-size: 1rem;
        }
        .btn-submit:hover {
            background: var(--primary-red-dark);
            border-color: var(--primary-red-dark);
            color: white;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(196, 30, 58, 0.3);
        }
        .btn-back {
            color: var(--dark-secondary);
            border-color: var(--border-color);
            padding: 8px 16px;
            font-size: 0.875rem;
            border-radius: 8px;
        }
        .btn-back:hover {
            background: var(--dark);
            border-color: var(--dark);
            color: white;
        }
        .header-icon {
            width: 40px;
            height: 40px;
            background: var(--primary-red);
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 12px;
        }
        .header-icon i {
            color: white;
            font-size: 1.1rem;
        }
    </style>
</head>
<body>
    <div class="form-card">
        <div class="d-flex justify-content-between align-items-start mb-4">
            <div class="d-flex align-items-center">
                <div class="header-icon">
                    <i class="fas fa-route"></i>
                </div>
                <div>
                    <h2 class="form-title mb-0">Planification</h2>
                    <small class="text-muted">Recherche automatique</small>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/pages/" class="btn btn-back">
                <i class="fas fa-arrow-left me-1"></i> Retour
            </a>
        </div>
        
        <p class="form-subtitle">
            <i class="fas fa-info-circle me-1"></i>
            Selectionnez une date pour lancer la planification automatique des vehicules.
        </p>
        
        <form id="searchForm">
            <div class="mb-4">
                <label for="date" class="form-label">
                    <i class="fas fa-calendar-alt me-1"></i>
                    Date de planification
                </label>
                <input type="date" class="form-control" id="date" name="date" required>
            </div>
            
            <div class="d-grid">
                <button type="submit" class="btn btn-submit">
                    <i class="fas fa-search me-2"></i>
                    Lancer la planification
                </button>
            </div>
        </form>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.getElementById('date').valueAsDate = new Date();
        
        document.getElementById('searchForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const date = document.getElementById('date').value;
            window.location.href = '${pageContext.request.contextPath}/pages/liste-planification?date=' + date;
        });
    </script>
</body>
</html>
