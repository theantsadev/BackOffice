<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.util.List" %>
        <%@ page import="com.hotel.model.Hotel" %>
            <!DOCTYPE html>
            <html lang="fr">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Nouvelle Réservation</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <style>
                    body {
                        /* background: linear-gradient(135deg, #17245f 0%, #0b0242 100%); */
                        background: linear-gradient(135deg, #5a5a5a 0%, #111111 100%);
                        min-height: 100vh;
                        padding: 40px 20px;
                    }

                    .form-card {
                        background: white;
                        border-radius: 15px;
                        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
                        padding: 40px;
                        max-width: 700px;
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

                    .alert-custom {
                        border-radius: 10px;
                        display: none;
                    }
                </style>
            </head>

            <body>
                <div class="form-card">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2 class="form-title mb-0">Nouvelle Réservation</h2>
                        <a href="${pageContext.request.contextPath}/pages/" class="btn btn-outline-secondary">Retour</a>
                    </div>

                    <div id="alertMessage" class="alert alert-custom"></div>

                    <form id="reservationForm">
                        <div class="mb-3">
                            <label for="id_client" class="form-label">ID Client</label>
                            <input type="text" class="form-control" id="id_client" name="id_client"
                                placeholder="Entrez l'identifiant client (4 chiffres)" pattern="[0-9]{4}" required>
                            <div class="form-text">Format: 4 chiffres (ex: 1234)</div>
                        </div>

                        <div class="mb-3">
                            <label for="nb_passager" class="form-label">Nombre de passagers</label>
                            <input type="number" class="form-control" id="nb_passager" name="nb_passager" min="1"
                                placeholder="Nombre de passagers" required>
                        </div>

                        <div class="mb-3">
                            <label for="date_heure_arrivee" class="form-label">Date et heure d'arrivée</label>
                            <input type="datetime-local" class="form-control" id="date_heure_arrivee"
                                name="date_heure_arrivee" required>
                        </div>

                        <div class="mb-4">
                            <label for="id_hotel" class="form-label">Hôtel</label>
                            <select class="form-select" id="id_hotel" name="id_hotel" required>
                                <option value="">Sélectionnez un hôtel</option>
                                <% List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
                                        if (hotels != null) {
                                        for (Hotel hotel : hotels) {
                                        %>
                                        <option value="<%= hotel.getId_hotel() %>">
                                            <%= hotel.getNom() %>
                                        </option>
                                        <% } } %>
                            </select>
                        </div>

                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary btn-submit">
                                Réserver
                            </button>
                        </div>
                    </form>
                </div>

                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                <script src="${pageContext.request.contextPath}/js/api-helper.js"></script>
                <script>
                    const contextPath = '${pageContext.request.contextPath}';

                    document.getElementById('reservationForm').addEventListener('submit', async function (e) {
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
                                    alertDiv.textContent = 'Réservation effectuée avec succès!';
                                    alertDiv.style.display = 'block';
                                    document.getElementById('reservationForm').reset();

                                    setTimeout(() => {
                                        window.location.href = contextPath + '/pages/liste-reservations';
                                    }, 1500);
                                },
                                (code, message) => {
                                    alertDiv.className = 'alert alert-danger alert-custom';
                                    alertDiv.textContent = 'Erreur: ' + message;
                                    alertDiv.style.display = 'block';
                                }
                            );
                        } catch (error) {
                            const alertDiv = document.getElementById('alertMessage');
                            alertDiv.className = 'alert alert-danger alert-custom';
                            alertDiv.textContent = 'Erreur lors de la réservation: ' + error.message;
                            alertDiv.style.display = 'block';
                        }
                    });
                </script>
            </body>

            </html>