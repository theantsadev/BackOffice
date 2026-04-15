-- ============================================================
-- TEST 2 - Scenario de validation de planification (image)
-- ============================================================
-- Objectif:
-- 1) V1 (10 places) disponible a 08:00
-- 2) V2 (10 places) disponible a 10:00
-- 3) Reservations:
--    C1: 14 pax a 08:00
--    C2: 7 pax a 10:00
--    C3: 4 pax a 10:20
--
-- Ce script prepare uniquement les donnees d'entree.
-- La table Planification doit rester vide avant de lancer la planification auto.

BEGIN;

-- Nettoyage scenario (schema deja existant)
TRUNCATE TABLE
    Planification,
    Regroupement,
    Reservation,
    Vehicule,
    Distance,
    Parametre,
    Hotel
RESTART IDENTITY CASCADE;

-- Hotels
INSERT INTO Hotel (id_hotel, nom) VALUES
(0, 'Aeroport'),
(1, 'hotel1'),
(2, 'hotel2');

-- Vehicules
INSERT INTO Vehicule (reference, place, type_carburant, heure_debut_dispo) VALUES
('V1', 10, 'D', '00:00:00'),
('V2', 8, 'D', '08:00:00'),
('V3', 8, 'E', '08:00:00'),
('V4', 12, 'E', '09:00:00');

-- Reservations
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
(20, '2026-04-02 06:00:00', 'C1', 1),
(6,  '2026-04-02 08:15:00', 'C2', 1),
(10,  '2026-04-02 09:00:00', 'C3', 1),
(6,  '2026-04-02 09:10:00', 'C4', 2);


-- Distance aeroport -> hotel1 (50 km)
INSERT INTO Distance (from_hotel, to_hotel, valeur) VALUES
(0, 1, 90),
(0, 2, 65),
(1, 2, 10);


-- Parametres
INSERT INTO Parametre (cle, valeur, unite) VALUES
('temps_attente_min', 30, 'min'),
('vitesse_moyenne_kmh', 60, 'km/h');

COMMIT;

-- ============================================================
-- Verification des donnees d'entree
-- ============================================================
SELECT * FROM Vehicule ORDER BY id;
SELECT * FROM Reservation ORDER BY date_heure_arrivee;
SELECT * FROM Parametre ORDER BY cle;

-- ============================================================
-- Resultat attendu apres lancement de la planification auto (02/04/2026)
-- ============================================================
-- Hypothese metier (selon ton exemple):
-- - V1 part a 08:00 avec 10 pax de C1 puis revient a 10:00
-- - A 10:00, V2 prend le reste de C1 (4 pax) + une partie de C2 (6 pax)
-- - A 10:20, V1 prend le reste de C2 (1 pax) + C3 (4 pax)
--
-- Verifier avec une requete de ce type APRES planification:
--
-- SELECT
--   v.reference AS vehicule,
--   r.id_client AS reservation_client,
--   p.nb_passager_assigne AS nb,
--   p.date_heure_depart_aeroport AS depart,
--   p.date_heure_retour_aeroport AS retour
-- FROM Planification p
-- JOIN Reservation r ON r.id_reservation = p.id_reservation
-- JOIN Vehicule v ON v.id = p.id_vehicule
-- ORDER BY p.date_heure_depart_aeroport, v.reference, r.id_client;
--
-- Lignes attendues (ordre logique):
-- 1) V1 | C1 | 10 | 08:00 | 10:00
-- 2) V2 | C1 |  4 | 10:00 | 12:00
-- 3) V2 | C2 |  6 | 10:00 | 12:00
-- 4) V1 | C2 |  1 | 10:20 | 12:20
-- 5) V1 | C3 |  4 | 10:20 | 12:20
