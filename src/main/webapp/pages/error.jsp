<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur</title>
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
        .error-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.08);
            padding: 48px;
            max-width: 500px;
            text-align: center;
            border: 1px solid var(--border-color);
        }
        .error-icon {
            width: 80px;
            height: 80px;
            background: rgba(196, 30, 58, 0.1);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 24px;
        }
        .error-icon i {
            font-size: 2rem;
            color: var(--primary-red);
        }
        .error-title {
            color: var(--dark);
            font-weight: 700;
            font-size: 1.5rem;
            margin-bottom: 16px;
        }
        .error-message {
            color: var(--dark-secondary);
            margin-bottom: 32px;
            font-size: 1rem;
            line-height: 1.6;
        }
        .btn-primary-custom {
            background: var(--primary-red);
            border-color: var(--primary-red);
            color: white;
            padding: 14px 32px;
            font-weight: 600;
            border-radius: 10px;
            font-size: 1rem;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 10px;
        }
        .btn-primary-custom:hover {
            background: var(--primary-red-dark);
            border-color: var(--primary-red-dark);
            color: white;
        }
    </style>
</head>
<body>
    <div class="error-card">
        <div class="error-icon">
            <i class="fas fa-exclamation-triangle"></i>
        </div>
        <h2 class="error-title">Une erreur est survenue</h2>
        <p class="error-message"><%= request.getAttribute("error") != null ? request.getAttribute("error") : "Une erreur inattendue s'est produite." %></p>
        <a href="${pageContext.request.contextPath}/pages/" class="btn btn-primary-custom">
            <i class="fas fa-home"></i>
            Retour a l'accueil
        </a>
    </div>
</body>
</html>
