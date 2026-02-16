-- Script d'insertion des hôtels (initialisation)
-- À exécuter après la création de la base de données

-- Insertion des hôtels
INSERT INTO Hotel (nom) VALUES 
    ('Hôtel Colbert'),
    ('Carlton Hotel'),
    ('Grand Hotel Tananarive'),
    ('Hotel Palissandre'),
    ('Louvre Hotel & Spa'),
    ('Hotel La Varangue'),
    ('Tana Hotel'),
    ('Radisson Blu Hotel'),
    ('Ibis Ankorondrano'),
    ('Tamboho Hotel');

-- Vérification
SELECT * FROM Hotel ORDER BY nom;

-- Insertion des véhicules
INSERT INTO Vehicule (reference, place, type_carburant) VALUES 
    ('VH-001', 4, 'E'),
    ('VH-002', 5, 'D'),
    ('VH-003', 7, 'D'),
    ('VH-004', 2, 'E'),
    ('VH-005', 9, 'D');

-- Vérification véhicules
SELECT * FROM Vehicule ORDER BY reference;
