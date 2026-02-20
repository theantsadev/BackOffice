-- Script de réinitialisation complète de la base de données
-- Supprime la base existante et la recrée avec les données initiales

-- Terminer les connexions existantes
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'projet_hotel'
  AND pid <> pg_backend_pid();

-- Supprimer la base de données existante
DROP DATABASE IF EXISTS projet_hotel;

-- Créer une nouvelle base de données
CREATE DATABASE projet_hotel;

-- Se connecter à la nouvelle base
\c projet_hotel;

-- ===========================================
-- CRÉATION DES TABLES
-- ===========================================

-- Table Hotel
CREATE TABLE Hotel(
   id_hotel SERIAL,
   nom VARCHAR(50),
   PRIMARY KEY(id_hotel)
);

-- Table Reservation
CREATE TABLE Reservation(
   id_reservation SERIAL,
   nb_passager INTEGER,
   date_heure_arrivee TIMESTAMP,
   id_client VARCHAR(50),
   id_hotel INTEGER NOT NULL,
   PRIMARY KEY(id_reservation),
   FOREIGN KEY(id_hotel) REFERENCES Hotel(id_hotel)
);

-- Table Vehicule
CREATE TABLE Vehicule (
   id SERIAL,
   reference VARCHAR(50) NOT NULL UNIQUE,
   place INTEGER NOT NULL,
   type_carburant CHAR(1) NOT NULL CHECK (type_carburant IN ('D', 'E')),
   PRIMARY KEY (id)
);

-- Table Token
CREATE TABLE Token (
   id SERIAL,
   token VARCHAR(255) NOT NULL UNIQUE,
   date_heure_expiration TIMESTAMP NOT NULL,
   PRIMARY KEY (id)
);

-- Table Parametre (accès par base uniquement, pas de CRUD)
CREATE TABLE Parametre (
   id_parametre SERIAL,
   cle VARCHAR(100) NOT NULL UNIQUE,
   valeur DOUBLE PRECISION NOT NULL,
   unite VARCHAR(50),
   PRIMARY KEY (id_parametre)
);

-- Table Distance
-- Note: La distance A→B = B→A. On n'insère qu'une seule direction (from_hotel < to_hotel par convention)
CREATE TABLE Distance (
   id_distance SERIAL,
   from_hotel INTEGER NOT NULL,
   to_hotel INTEGER NOT NULL,
   valeur DOUBLE PRECISION NOT NULL,
   PRIMARY KEY (id_distance),
   CONSTRAINT fk_from_hotel FOREIGN KEY (from_hotel) REFERENCES Hotel(id_hotel),
   CONSTRAINT fk_to_hotel FOREIGN KEY (to_hotel) REFERENCES Hotel(id_hotel),
   CONSTRAINT unique_distance UNIQUE (from_hotel, to_hotel),
   CONSTRAINT check_from_lt_to CHECK (from_hotel < to_hotel)
);

-- Table Planification
CREATE TABLE Planification (
   id_planification SERIAL,
   id_reservation INTEGER NOT NULL,
   id_vehicule INTEGER NOT NULL,
   date_heure_depart_aeroport TIMESTAMP NOT NULL,
   date_heure_retour_aeroport TIMESTAMP NOT NULL,
   PRIMARY KEY (id_planification),
   CONSTRAINT fk_reservation FOREIGN KEY (id_reservation) REFERENCES Reservation(id_reservation),
   CONSTRAINT fk_vehicule FOREIGN KEY (id_vehicule) REFERENCES Vehicule(id)
);

-- ===========================================
-- INSERTION DES DONNÉES
-- ===========================================

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
-- Convention: from_hotel < to_hotel (distance A→B = B→A)
-- Aéroport (id=0) vers les hôtels
INSERT INTO Distance (from_hotel, to_hotel, valeur) VALUES
    (0, 1, 15.0),   -- Aéroport -> Colbert
    (0, 2, 10.0),   -- Aéroport -> Novotel
    (0, 3, 8.0),    -- Aéroport -> Ibis
    (0, 4, 20.0);   -- Aéroport -> Lokanga

-- Distances entre hôtels
INSERT INTO Distance (from_hotel, to_hotel, valeur) VALUES
    (1, 2, 5.0),    -- Colbert -> Novotel
    (1, 3, 7.0),    -- Colbert -> Ibis
    (1, 4, 12.0),   -- Colbert -> Lokanga
    (2, 3, 4.0),    -- Novotel -> Ibis
    (2, 4, 15.0),   -- Novotel -> Lokanga
    (3, 4, 18.0);   -- Ibis -> Lokanga

-- ===========================================
-- VÉRIFICATION
-- ===========================================
SELECT * FROM Hotel ORDER BY id_hotel;
SELECT * FROM Reservation ORDER BY id_reservation;
SELECT * FROM Vehicule ORDER BY id;
SELECT * FROM Parametre ORDER BY cle;
SELECT d.*, h1.nom AS from_hotel_nom, h2.nom AS to_hotel_nom 
FROM Distance d
JOIN Hotel h1 ON d.from_hotel = h1.id_hotel
JOIN Hotel h2 ON d.to_hotel = h2.id_hotel
ORDER BY from_hotel, to_hotel;
