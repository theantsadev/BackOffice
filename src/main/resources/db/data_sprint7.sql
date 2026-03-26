-- ============================================================
-- RESET COMPLET
-- ============================================================
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'projet_hotel'
  AND pid <> pg_backend_pid();

DROP DATABASE IF EXISTS projet_hotel;
CREATE DATABASE projet_hotel;
\c projet_hotel;

-- ============================================================
-- TABLES
-- ============================================================

CREATE TABLE Hotel(
   id_hotel INTEGER PRIMARY KEY,
   nom VARCHAR(50)
);

CREATE TABLE Reservation(
   id_reservation SERIAL PRIMARY KEY,
   nb_passager INTEGER,
   date_heure_arrivee TIMESTAMP,
   id_client VARCHAR(50),
   id_hotel INTEGER REFERENCES Hotel(id_hotel)
);

CREATE TABLE Vehicule (
   id SERIAL PRIMARY KEY,
   reference VARCHAR(50) UNIQUE,
   place INTEGER,
   type_carburant CHAR(1),
   heure_debut_dispo TIME
);

CREATE TABLE Parametre (
   id_parametre SERIAL PRIMARY KEY,
   cle VARCHAR(100) UNIQUE,
   valeur DOUBLE PRECISION,
   unite VARCHAR(50)
);

CREATE TABLE Distance (
   id_distance SERIAL PRIMARY KEY,
   from_hotel INTEGER,
   to_hotel INTEGER,
   valeur DOUBLE PRECISION
);

CREATE TABLE Planification (
   id_planification SERIAL PRIMARY KEY,
   id_reservation INTEGER REFERENCES Reservation(id_reservation),
   id_vehicule INTEGER REFERENCES Vehicule(id),
   date_heure_depart_aeroport TIMESTAMP,
   date_heure_retour_aeroport TIMESTAMP,
   nb_passager_assigne INTEGER
);


CREATE TABLE Token (
   id SERIAL,
   token VARCHAR(255) NOT NULL UNIQUE,
   date_heure_expiration TIMESTAMP NOT NULL,
   PRIMARY KEY (id)
);


-- ============================================================
-- DONNÉES
-- ============================================================

-- HÔTELS
INSERT INTO Hotel VALUES
(0, 'Aeroport'),
(1, 'hotel1'),
(2, 'hotel2');

-- ============================================================
-- VÉHICULES (comme image + correction dispo)
-- ============================================================

INSERT INTO Vehicule (reference, place, type_carburant, heure_debut_dispo) VALUES
('vehicule1', 5, 'D', '09:00:00'),
('vehicule2', 5, 'E', '09:00:00'),
('vehicule3', 12, 'D', '08:00:00'),
('vehicule4', 9, 'D', '09:00:00'),
('vehicule5', 12, 'E', '13:00:00');

-- ============================================================
-- RÉSERVATIONS (exact image)
-- ============================================================

INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
(7,  '2026-03-19 09:00:00', 'Client1', 1),
(20, '2026-03-19 08:00:00', 'Client2', 2),
(3,  '2026-03-19 09:10:00', 'Client3', 1),
(10, '2026-03-19 09:15:00', 'Client4', 1),
(5,  '2026-03-19 09:20:00', 'Client5', 1),
(12, '2026-03-19 13:30:00', 'Client6', 1);

-- ============================================================
-- DISTANCES (image)
-- ============================================================

INSERT INTO Distance (from_hotel, to_hotel, valeur) VALUES
(0, 1, 90),
(0, 2, 35),
(1, 2, 60);

-- ============================================================
-- PARAMÈTRES
-- ============================================================

INSERT INTO Parametre (cle, valeur, unite) VALUES
('temps_attente_min', 30, 'min'),
('vitesse_moyenne_kmh', 50, 'km/h');

-- ============================================================
-- VÉRIFICATION
-- ============================================================

-- Réservations
SELECT * FROM Reservation ORDER BY date_heure_arrivee;

-- Véhicules
SELECT * FROM Vehicule;

-- Distances
SELECT * FROM Distance;

-- Paramètres
SELECT * FROM Parametre;