-- Script d'insertion des hôtels (initialisation)
-- À exécuter après la création de la base de données

-- Insertion des hôtels
INSERT INTO Hotel (id_hotel, nom) VALUES 
    (1, 'Colbert'),
    (2, 'Novotel'),
    (3, 'Ibis'),
    (4, 'Lokanga');

-- Vérification
SELECT * FROM Hotel ORDER BY nom;
