<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #17245f 0%, #0b0242 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .error-card {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
            padding: 40px;
            max-width: 500px;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="error-card">
        <h2 class="text-danger mb-3">Erreur</h2>
        <p class="text-muted mb-4"><%= request.getAttribute("error") %></p>
        <a href="/" class="btn btn-primary">Retour à l'accueil</a>
    </div>
</body>
</html>
