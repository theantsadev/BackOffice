-- Script de réinitialisation des données (supprime toutes les données sans supprimer les tables)
-- Ordre de suppression important pour respecter les contraintes de clés étrangères

\c projet_hotel;

-- Suppression des données (ordre inverse des dépendances)
DELETE FROM Planification;
DELETE FROM Distance;
DELETE FROM Parametre;
DELETE FROM Token;
DELETE FROM Vehicule;
DELETE FROM Reservation;
DELETE FROM Hotel;

-- Réinitialisation des séquences
ALTER SEQUENCE planification_id_planification_seq RESTART WITH 1;
ALTER SEQUENCE distance_id_distance_seq RESTART WITH 1;
ALTER SEQUENCE parametre_id_parametre_seq RESTART WITH 1;
ALTER SEQUENCE token_id_seq RESTART WITH 1;
ALTER SEQUENCE vehicule_id_seq RESTART WITH 1;
ALTER SEQUENCE reservation_id_reservation_seq RESTART WITH 1;
ALTER SEQUENCE hotel_id_hotel_seq RESTART WITH 1;

-- Ré-insertion des données initiales

-- Insertion de l'aéroport (id = 0)
INSERT INTO Hotel (id_hotel, nom) VALUES (0, 'Aeroport');

-- Insertion des hôtels
INSERT INTO Hotel (id_hotel, nom) VALUES 
    (1, 'Colbert'),
    (2, 'Novotel'),
    (3, 'Ibis'),
    (4, 'Lokanga');

-- Insertion des réservations
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES 
    ( 11, '2026-02-05 00:01:00', '4631', 3),
    ( 1, '2026-02-05 23:55:00', '4394', 3),
    ( 2, '2026-02-09 10:17:00', '8054', 1),
    ( 4, '2026-02-01 15:25:00', '1432', 2),
    ( 4, '2026-01-28 07:11:00', '7861', 1),
    ( 5, '2026-01-28 07:45:00', '3308', 1),
    ( 13, '2026-02-28 08:25:00', '4484', 2),
    ( 8, '2026-02-28 13:00:00', '9687', 2),
    ( 7, '2026-02-15 13:00:00', '6302', 1),
    ( 1, '2026-02-18 22:55:00', '8640', 4);

-- Insertion des véhicules
INSERT INTO Vehicule (reference, place, type_carburant) VALUES
    ('VH-001', 4, 'E'),
    ('VH-002', 5, 'D'),
    ('VH-003', 7, 'D'),
    ('VH-004', 2, 'E'),
    ('VH-005', 9, 'D');

-- Insertion des paramètres initiaux
INSERT INTO Parametre (cle, valeur, unite) VALUES
    ('vitesse_moyenne_kmh', 30, 'km/h'),
    ('temps_attente_min', 30, 'min');

-- Insertion des distances (en km)
INSERT INTO Distance (from_hotel, to_hotel, valeur) VALUES
    (0, 1, 15.0),   -- Aéroport -> Colbert
    (0, 2, 10.0),   -- Aéroport -> Novotel
    (0, 3, 8.0),    -- Aéroport -> Ibis
    (0, 4, 20.0);   -- Aéroport -> Lokanga

INSERT INTO Distance (from_hotel, to_hotel, valeur) VALUES
    (1, 2, 5.0),    -- Colbert -> Novotel
    (1, 3, 7.0),    -- Colbert -> Ibis
    (1, 4, 12.0),   -- Colbert -> Lokanga
    (2, 3, 4.0),    -- Novotel -> Ibis
    (2, 4, 15.0),   -- Novotel -> Lokanga
    (3, 4, 18.0);   -- Ibis -> Lokanga

-- Vérification
SELECT 'Hotel' AS table_name, COUNT(*) AS count FROM Hotel
UNION ALL SELECT 'Reservation', COUNT(*) FROM Reservation
UNION ALL SELECT 'Vehicule', COUNT(*) FROM Vehicule
UNION ALL SELECT 'Parametre', COUNT(*) FROM Parametre
UNION ALL SELECT 'Distance', COUNT(*) FROM Distance
UNION ALL SELECT 'Planification', COUNT(*) FROM Planification;
