-- projet_hotel=# select * from hotel;
--  id_hotel |   nom
-- ----------+----------
--         0 | Aeroport
--         1 | Colbert
--         2 | Novotel
--         3 | Ibis
--         4 | Lokanga
--         5 | Gare

-- projet_hotel=# select * from vehicule ;
--  id | reference | place | type_carburant
-- ----+-----------+-------+----------------
--   1 | VH-001    |    12 | D
--   2 | VH-002    |     5 | E
--   4 | VH-004    |    12 | E
--   3 | VH-003    |     4 | D


INSERT INTO
    Reservation (
        nb_passager,
        date_heure_arrivee,
        id_client,
        id_hotel
    )
VALUES
    (12, '2026-03-17 08:00:00', 'CLT01', 1),
    (5, '2026-03-17 08:15:00', 'CLT02', 2),
    (12, '2026-03-17 08:20:00', 'CLT03', 3),
    (5, '2026-03-17 08:25:00', 'CLT04', 3),
    (1, '2026-03-17 09:24:00', 'CLT05', 3),
    (1, '2026-03-17 09:40:00', 'CLT06', 3);

    