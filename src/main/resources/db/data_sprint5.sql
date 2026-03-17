-- projet_hotel=# select * from hotel;
--  id_hotel |   nom
-- ----------+----------
--         0 | Aeroport
--         1 | Colbert
--         2 | Novotel
--         3 | Ibis
--         4 | Lokanga
--         5 | Gare
INSERT INTO
    Reservation (
        nb_passager,
        date_heure_arrivee,
        id_client,
        id_hotel
    )
VALUES
    (11, '2026-03-17 08:00:00', 'CLT01', 1),
    (1, '2026-03-17 08:15:00', 'CLT02', 2),
    (20, '2026-03-17 08:20:00', 'CLT03', 3),
    (1, '2026-03-17 09:15:00', 'CLT04', 3),
    (1, '2026-03-17 09:24:00', 'CLT05', 3),
    (1, '2026-03-17 09:40:00', 'CLT06', 3);

    