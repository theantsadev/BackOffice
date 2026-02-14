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

-- Créer les tables
CREATE TABLE Hotel(
   id_hotel SERIAL,
   nom VARCHAR(50),
   PRIMARY KEY(id_hotel)
);

CREATE TABLE Reservation(
   id_reservation SERIAL,
   nb_passager INTEGER,
   date_heure_arrivee TIMESTAMP,
   id_client VARCHAR(50),
   id_hotel INTEGER NOT NULL,
   PRIMARY KEY(id_reservation),
   FOREIGN KEY(id_hotel) REFERENCES Hotel(id_hotel)
);

-- Insertion des hôtels
INSERT INTO Hotel (id_hotel, nom) VALUES 
    (1, 'Colbert'),
    (2, 'Novotel'),
    (3, 'Ibis'),
    (4, 'Lokanga');

-- Insertion des réservations
INSERT INTO Reservation (id_reservation, nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES 
    (1, 11, '2026-02-05 00:01:00', '4631', 3),
    (2, 1, '2026-02-05 23:55:00', '4394', 3),
    (3, 2, '2026-02-09 10:17:00', '8054', 1),
    (4, 4, '2026-02-01 15:25:00', '1432', 2),
    (5, 4, '2026-01-28 07:11:00', '7861', 1),
    (6, 5, '2026-01-28 07:45:00', '3308', 1),
    (7, 13, '2026-02-28 08:25:00', '4484', 2),
    (8, 8, '2026-02-28 13:00:00', '9687', 2),
    (9, 7, '2026-02-15 13:00:00', '6302', 1),
    (10, 1, '2026-02-18 22:55:00', '8640', 4);

-- Vérification
SELECT * FROM Hotel ORDER BY id_hotel;
SELECT * FROM Reservation ORDER BY id_reservation;
