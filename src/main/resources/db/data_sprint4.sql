-- =============================================================
-- data_sprint4.sql
-- Données de test — Sprint 4
--   F1 : Covoiturage automatique (plusieurs clients, même voiture)
--   F2 : Ordre de dépôt par distance aéroport
--
-- Date de test : 2026-03-15
-- Appel API    : GET /planifications?date=2026-03-15
-- =============================================================
--
-- Rappel véhicules (V5__insert_test_vehicules.sql) :
--   VH-001 :  4 places, Essence
--   VH-002 :  5 places, Diesel
--   VH-003 :  7 places, Diesel
--   VH-004 :  2 places, Essence
--   VH-005 :  9 places, Diesel
--
-- Rappel distances aéroport (id=0) → hôtel :
--   Ibis     (id=3) :  8 km  → retour =  +32 min
--   Novotel  (id=2) : 10 km  → retour =  +40 min
--   Colbert  (id=1) : 15 km  → retour =  +60 min
--   Lokanga  (id=4) : 20 km  → retour =  +80 min
-- =============================================================


-- =============================================================
-- F1 — COVOITURAGE (même heure + même hôtel → même voiture)
-- =============================================================

-- Scénario F1-1 : 08h00, Ibis
--   6 pax → VH-003 (7 places Diesel, 1 place restante)
--   1 pax → rentre dans VH-003 (covoiturage)
--   Résultat attendu : badge "🚌 2 clients" sur les 2 lignes
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (6, '2026-03-15 08:00:00', 'SP4-F1A', 3),
    (1, '2026-03-15 08:00:00', 'SP4-F1B', 3);


-- Scénario F1-2 : 11h00, Novotel  (FFD tri desc : 3, 2, 1)
--   3 pax → VH-001 (4 places Essence, 1 place restante)
--   2 pax → VH-001 plein (1 < 2) → ouvre VH-004 (2 places Essence)
--   1 pax → 1 place restante dans VH-001 → covoiturage avec SP4-F1C
--   Résultat attendu :
--     VH-001 : SP4-F1C (3 pax) + SP4-F1D (1 pax) → badge "🚌 2 clients"
--     VH-004 : SP4-F1E (2 pax)                  → badge "-"

INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (3, '2026-03-15 11:00:00', 'SP4-F1C', 2),
    (1, '2026-03-15 11:00:00', 'SP4-F1D', 2),
    (2, '2026-03-15 11:00:00', 'SP4-F1E', 2);


-- =============================================================
-- F2 — ORDRE DE DÉPÔT (distance aéroport croissante)
-- =============================================================

-- Scénario F2-1 : 14h00, 4 hôtels différents (1 voiture par hôtel)
--   Chaque voiture a son propre itinéraire avec la distance affichée.
--   Les cartes d'itinéraire montrent visiblement que chaque véhicule
--   dessert une distance différente depuis l'aéroport.
--   Résultat attendu :
--     VH-004 → Colbert  (2 pax, 15 km) — ordreDepot 1
--     VH-001 → Novotel  (1 pax, 10 km) — ordreDepot 1
--     VH-002 → Ibis     (1 pax,  8 km) — ordreDepot 1
--     VH-003 → Lokanga  (1 pax, 20 km) — ordreDepot 1
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (2, '2026-03-15 14:00:00', 'SP4-F2A', 1),   -- Colbert  (15 km)
    (1, '2026-03-15 14:00:00', 'SP4-F2B', 2),   -- Novotel  (10 km)
    (1, '2026-03-15 14:00:00', 'SP4-F2C', 3),   -- Ibis     ( 8 km)
    (1, '2026-03-15 14:00:00', 'SP4-F2D', 4);   -- Lokanga  (20 km)


-- Scénario F2-2 : 16h00, multi-stop dans la même voiture (auto-planifié)
--   SP4-F2E (3 pax) → Ibis    ( 8 km)  ordreDepot = 1  ← plus proche
--   SP4-F2F (2 pax) → Novotel (10 km)  ordreDepot = 2  ← plus éloigné
--   Total = 5 pax → VH-002 (5 places Diesel) ou VH-003 (7D) selon disponibilité
--   La planification est créée automatiquement au clic "Rechercher".
--   L'itinéraire affichera : Aéroport → Ibis (8 km) → Novotel (10 km)

INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (3, '2026-03-15 16:00:00', 'SP4-F2E', 3),   
    (2, '2026-03-15 16:00:00', 'SP4-F2F', 2);   


-- =============================================================
-- VÉRIFICATION
-- =============================================================
SELECT id_reservation, id_client, nb_passager, date_heure_arrivee, id_hotel
FROM Reservation
WHERE id_client LIKE 'SP4-%'
ORDER BY date_heure_arrivee, id_hotel;

SELECT p.id_planification, p.id_reservation, r.id_client, v.reference,
       p.date_heure_depart_aeroport, p.date_heure_retour_aeroport
FROM Planification p
JOIN Reservation r ON r.id_reservation = p.id_reservation
JOIN Vehicule v ON v.id = p.id_vehicule
WHERE r.id_client LIKE 'SP4-%'
ORDER BY p.date_heure_depart_aeroport;
