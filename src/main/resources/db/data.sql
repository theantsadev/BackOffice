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
