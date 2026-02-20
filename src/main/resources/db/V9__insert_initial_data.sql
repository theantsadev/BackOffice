-- Insertion de l'aéroport comme hôtel spécial (id = 0)
INSERT INTO Hotel (id_hotel, nom) VALUES (0, 'Aeroport');

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

-- Distances entre hôtels (optionnel pour le Sprint 3)
INSERT INTO Distance (from_hotel, to_hotel, valeur) VALUES
    (1, 2, 5.0),    -- Colbert -> Novotel
    (1, 3, 7.0),    -- Colbert -> Ibis
    (1, 4, 12.0),   -- Colbert -> Lokanga
    (2, 3, 4.0),    -- Novotel -> Ibis
    (2, 4, 15.0),   -- Novotel -> Lokanga
    (3, 4, 18.0);   -- Ibis -> Lokanga

-- Vérification
SELECT * FROM Parametre ORDER BY cle;
SELECT d.*, h1.nom AS from_hotel_nom, h2.nom AS to_hotel_nom 
FROM Distance d
JOIN Hotel h1 ON d.from_hotel = h1.id_hotel
JOIN Hotel h2 ON d.to_hotel = h2.id_hotel
ORDER BY from_hotel, to_hotel;
