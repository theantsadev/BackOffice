<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recherche Planifications</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #5a5a5a 0%, #111111 100%);
            min-height: 100vh;
            padding: 40px 20px;
        }
        .form-card {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
            padding: 40px;
            max-width: 600px;
            margin: 0 auto;
        }
        .form-title {
            color: #17245f;
            margin-bottom: 30px;
            font-weight: 700;
        }
        .form-label {
            font-weight: 500;
            color: #272727;
        }
        .btn-submit {
            padding: 12px 40px;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="form-card">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="form-title mb-0">Recherche Planifications</h2>
            <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-secondary">Retour</a>
        </div>
        
        <form id="searchForm">
            <div class="mb-4">
                <label for="date" class="form-label">Date de planification</label>
                <input type="date" class="form-control" id="date" name="date" required>
            </div>
            
            <div class="d-grid">
                <button type="submit" class="btn btn-primary btn-submit">
                    Rechercher
                </button>
            </div>
        </form>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Définir la date du jour par défaut
        document.getElementById('date').valueAsDate = new Date();
        
        document.getElementById('searchForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const date = document.getElementById('date').value;
            window.location.href = '${pageContext.request.contextPath}/pages/liste-planification?date=' + date;
        });
    </script>
</body>
</html>
