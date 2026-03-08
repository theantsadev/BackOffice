<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.hotel.model.Hotel" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nouvelle Reservation</title>
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
            max-width: 600px;
            margin: 0 auto;
            border: 1px solid var(--border-color);
        }
        .form-title {
            color: var(--dark);
            font-weight: 700;
            font-size: 1.4rem;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .form-title i {
            color: var(--primary-red);
        }
        .form-label {
            font-weight: 600;
            color: var(--dark);
            font-size: 0.9rem;
        }
        .form-control, .form-select {
            border: 1px solid var(--border-color);
            border-radius: 10px;
            padding: 12px 16px;
            transition: all 0.2s;
        }
        .form-control:focus, .form-select:focus {
            border-color: var(--primary-red);
            box-shadow: 0 0 0 3px rgba(196, 30, 58, 0.1);
        }
        .btn-primary-custom {
            background: var(--primary-red);
            border-color: var(--primary-red);
            color: white;
            padding: 14px 32px;
            font-weight: 600;
            border-radius: 10px;
            font-size: 1rem;
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
        .alert-custom {
            border-radius: 10px;
            display: none;
        }
        .form-text {
            color: #6c757d;
            font-size: 0.8rem;
        }
    </style>
</head>
<body>
    <div class="form-card">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="form-title mb-0">
                <i class="fas fa-calendar-plus"></i>
                Nouvelle Reservation
            </h2>
            <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-custom">
                <i class="fas fa-arrow-left me-2"></i>Retour
            </a>
        </div>
        
        <div id="alertMessage" class="alert alert-custom"></div>
        
        <form id="reservationForm">
            <div class="mb-3">
                <label for="id_client" class="form-label">
                    <i class="fas fa-user me-2" style="color: var(--primary-red);"></i>ID Client
                </label>
                <input type="text" class="form-control" id="id_client" name="id_client" 
                       placeholder="Entrez l'identifiant client (4 chiffres)" 
                       pattern="[0-9]{4}" required>
                <div class="form-text">Format: 4 chiffres (ex: 1234)</div>
            </div>
            
            <div class="mb-3">
                <label for="nb_passager" class="form-label">
                    <i class="fas fa-users me-2" style="color: var(--primary-red);"></i>Nombre de passagers
                </label>
                <input type="number" class="form-control" id="nb_passager" name="nb_passager" 
                       min="1" placeholder="Nombre de passagers" required>
            </div>
            
            <div class="mb-3">
                <label for="date_heure_arrivee" class="form-label">
                    <i class="fas fa-clock me-2" style="color: var(--primary-red);"></i>Date et heure d'arrivee
                </label>
                <input type="datetime-local" class="form-control" id="date_heure_arrivee" 
                       name="date_heure_arrivee" required>
            </div>
            
            <div class="mb-4">
                <label for="id_hotel" class="form-label">
                    <i class="fas fa-hotel me-2" style="color: var(--primary-red);"></i>Hotel
                </label>
                <select class="form-select" id="id_hotel" name="id_hotel" required>
                    <option value="">Selectionnez un hotel</option>
                    <%
                        List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
                        if (hotels != null) {
                            for (Hotel hotel : hotels) {
                    %>
                                <option value="<%= hotel.getId_hotel() %>"><%= hotel.getNom() %></option>
                    <%
                            }
                        }
                    %>
                </select>
            </div>
            
            <div class="d-grid">
                <button type="submit" class="btn btn-primary-custom">
                    <i class="fas fa-check me-2"></i>Reserver
                </button>
            </div>
        </form>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        
        document.getElementById('reservationForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const urlEncodedData = new URLSearchParams(formData);
            
            try {
                const result = await fetchApi(contextPath + '/reservations', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: urlEncodedData
                });
                
                const alertDiv = document.getElementById('alertMessage');
                
                handleApiResponse(result,
                    () => {
                        alertDiv.className = 'alert alert-success alert-custom';
                        alertDiv.innerHTML = '<i class="fas fa-check-circle me-2"></i>Reservation effectuee avec succes!';
                        alertDiv.style.display = 'block';
                        document.getElementById('reservationForm').reset();
                        
                        setTimeout(() => {
                            window.location.href = contextPath + '/pages/liste-reservations';
                        }, 1500);
                    },
                    (code, message) => {
                        alertDiv.className = 'alert alert-danger alert-custom';
                        alertDiv.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i>Erreur: ' + message;
                        alertDiv.style.display = 'block';
                    }
                );
            } catch (error) {
                const alertDiv = document.getElementById('alertMessage');
                alertDiv.className = 'alert alert-danger alert-custom';
                alertDiv.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i>Erreur lors de la reservation: ' + error.message;
                alertDiv.style.display = 'block';
            }
        });
    </script>
</body>
</html>
