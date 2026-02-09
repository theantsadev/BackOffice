-- Script de test pour vérifier les données

-- Afficher tous les hôtels
SELECT 'HOTELS:' as info;
SELECT * FROM Hotel ORDER BY nom;

-- Compter les hôtels
SELECT COUNT(*) as total_hotels FROM Hotel;

-- Afficher toutes les réservations
SELECT 'RESERVATIONS:' as info;
SELECT 
    r.id_reservation,
    r.id_client,
    r.nb_passager,
    r.date_heure_arrivee,
    h.nom as hotel_nom
FROM Reservation r
LEFT JOIN Hotel h ON r.id_hotel = h.id_hotel
ORDER BY r.date_heure_arrivee DESC;

-- Compter les réservations
SELECT COUNT(*) as total_reservations FROM Reservation;

-- Réservations par hôtel
SELECT 'RESERVATIONS PAR HOTEL:' as info;
SELECT 
    h.nom as hotel,
    COUNT(r.id_reservation) as nombre_reservations
FROM Hotel h
LEFT JOIN Reservation r ON h.id_hotel = r.id_hotel
GROUP BY h.id_hotel, h.nom
ORDER BY nombre_reservations DESC;
