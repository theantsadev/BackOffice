-- Insertion des véhicules
INSERT INTO
    Vehicule (reference, place, type_carburant)
VALUES
    ('VH-001', 4, 'E'),
    ('VH-002', 5, 'D'),
    ('VH-003', 7, 'D'),
    ('VH-004', 2, 'E'),
    ('VH-005', 9, 'D');

-- Vérification véhicules
SELECT
    *
FROM
    Vehicule
ORDER BY
    reference;