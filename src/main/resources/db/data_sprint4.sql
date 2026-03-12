-- Active: 1769014177397@@127.0.0.1@5433@projet_hotel
-- =============================================================
-- data_sprint4.sql — Données de test Sprint 4
-- Date de test : 2026-03-15
-- Appel API    : GET /planifications?date=2026-03-15
-- -------------------------------------------------------------
-- Rappel véhicules (après clear.sql) :
--   VH-001 : id=1,  4 places, Essence (E)
--   VH-002 : id=2,  5 places, Diesel  (D)
--   VH-003 : id=3,  7 places, Diesel  (D)
--   VH-004 : id=4,  2 places, Essence (E)
--   VH-005 : id=5,  9 places, Diesel  (D)
--
-- Rappel hôtels & distances depuis l'aéroport (id=0) :
--   Ibis     (id=3) :  8 km   retour estimé ≈ +62 min  (30 att + 2×16 traj)
--   Novotel  (id=2) : 10 km   retour estimé ≈ +70 min  (30 att + 2×20 traj)
--   Colbert  (id=1) : 15 km   retour estimé ≈ +90 min  (30 att + 2×30 traj)
--   Lokanga  (id=4) : 20 km   retour estimé ≈ +110 min (30 att + 2×40 traj)
--
-- Paramètres : vitesse = 30 km/h  |  temps_attente = 30 min
-- =============================================================

-- ┌─────────────────────────────────────────────────────────────────┐
-- │  RÈGLES DE GESTION TESTÉES                                      │
-- │                                                                 │
-- │  R1  : Un véhicule peut transporter N réservations en même temps│
-- │  R2  : FFD — traiter les plus grands groupes en premier         │
-- │  R3  : Seules les resa du MÊME horaire partagent un véhicule    │
-- │  R4a : Nouveau véhicule → Best-Fit (cap min ≥ pax) + Diesel > E│
-- │  R4b : Priorité véhicule partiellement chargé avant R4a        │
-- │  R5  : Dépôt : distance aéroport croissante ; ex-æquo → alpha  │
-- └─────────────────────────────────────────────────────────────────┘


-- =============================================================
-- SETUP : données de test supplémentaires
-- =============================================================

-- VH-006 : 4 places, Diesel — jumeau capacité de VH-001 (4 places, Essence).
-- Rôle   : permet le test de tie-break Diesel > Essence (R4a-TIE, Scénario 3).
-- Après clear.sql (vehicule_id_seq remis à 1 + 5 inserts auto), id=6 est attribué.
INSERT INTO Vehicule (reference, place, type_carburant) VALUES ('VH-006', 4, 'D');

-- Hôtel "Gare" (id=5) à 8 km de l'aéroport = même distance qu'Ibis.
-- Rôle : permet le test de tie-break alphabétique dans l'ordre de dépôt (R5-ALPHA, Scénario 5B).
INSERT INTO Hotel (id_hotel, nom) VALUES (5, 'Gare');
INSERT INTO Distance (from_hotel, to_hotel, valeur) VALUES (0, 5, 8.0);  -- Aéroport → Gare (8 km)
INSERT INTO Distance (from_hotel, to_hotel, valeur) VALUES (3, 5, 3.0);  -- Ibis     ↔ Gare (3 km)


-- =============================================================
-- SCÉNARIO 1 — R1 + R2 + R4a-TIE + R4b
--              FFD → Best-Fit + Diesel tie-break → covoiturage
-- =============================================================
-- Horaire : 2026-03-15 08:00  |  Hôtel : Ibis (id=3, 8 km)
--
-- FFD sort desc : SP4-S1A (3 pax) → SP4-S1B (1 pax)
--
-- Assignation :
--   ① SP4-S1A (3 pax) — aucun véhicule partiel au départ.
--       Best-Fit : cap ≥ 3 → taille minimale disponible = 4.
--         VH-001 (4 Essence)  ← ex-æquo capacité
--         VH-006 (4 Diesel)   ← ex-æquo capacité
--         Tie-break Diesel → VH-006 retenu. Restant VH-006 : 4 − 3 = 1 place.
--
--   ② SP4-S1B (1 pax) — véhicule partiel existant ?
--       VH-006 est partiel (1 restant), même horaire 08h00, 1 ≥ 1 → COVOITURAGE (R4b).
--       → SP4-S1B → VH-006.
--
-- ✅ Résultat attendu :
--     VH-006 : SP4-S1A (3 pax) + SP4-S1B (1 pax)  → badge "🚌 2 clients"
--     VH-001 : non utilisé à ce créneau.
-- =============================================================
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (3, '2026-03-15 08:00:00', 'SP4-S1A', 3),
    (1, '2026-03-15 08:00:00', 'SP4-S1B', 3);


-- =============================================================
-- SCÉNARIO 2 — R2 + R4a + R4b
--              FFD → débordement sur nouveau véhicule → covoiturage croisé
-- =============================================================
-- Horaire : 2026-03-15 10:00  |  Hôtel : Novotel (id=2, 10 km)
-- (VH-006 de retour de 08h00/Ibis ≈ 09h02 → disponible à 10h00 ✓)
--
-- FFD sort desc : SP4-S2A (4) → SP4-S2B (3) → SP4-S2C (1)
--
-- Assignation :
--   ① SP4-S2A (4 pax) — aucun partiel.
--       Best-Fit cap ≥ 4 → taille min = 4 → VH-001(4E) et VH-006(4D) ex-æquo.
--       Diesel → VH-006. Restant : 4 − 4 = 0 (plein).
--
--   ② SP4-S2B (3 pax) — VH-006 plein (0 restant), aucun autre partiel.
--       Best-Fit cap ≥ 3 → taille min = 4 → seul VH-001 (4E) disponible.
--       → SP4-S2B → VH-001. Restant : 4 − 3 = 1.
--
--   ③ SP4-S2C (1 pax) — VH-001 partiel (1 restant), même horaire → COVOITURAGE (R4b).
--       → SP4-S2C → VH-001.
--
-- ✅ Résultat attendu :
--     VH-006 : SP4-S2A (4 pax)                         → 1 client, plein
--     VH-001 : SP4-S2B (3 pax) + SP4-S2C (1 pax)       → badge "🚌 2 clients"
-- =============================================================
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (4, '2026-03-15 10:00:00', 'SP4-S2A', 2),
    (3, '2026-03-15 10:00:00', 'SP4-S2B', 2),
    (1, '2026-03-15 10:00:00', 'SP4-S2C', 2);


-- =============================================================
-- SCÉNARIO 3 — R4a-TIE (isolé)
--              Tie-break Diesel > Essence à capacité égale
-- =============================================================
-- Horaire : 2026-03-15 12:00  |  Hôtel : Colbert (id=1, 15 km)
-- (VH-001 de retour de 10h00/Novotel ≈ 11h10 → disponible ✓)
-- (VH-006 de retour de 10h00/Novotel ≈ 11h10 → disponible ✓)
--
-- Seule réservation à ce créneau : SP4-S3A (3 pax).
--   Aucun partiel. Best-Fit cap ≥ 3 → taille minimale = 4.
--     Candidats à capacité 4 :
--       VH-001 (4 Essence)  ←── éliminé par tie-break
--       VH-006 (4 Diesel)   ←── RETENU
--     VH-002 (5D), VH-003 (7D), VH-005 (9D) : capacité PLUS grande → non Best-Fit.
--
-- ✅ Résultat attendu :
--     VH-006 : SP4-S3A (3 pax) ← Diesel préféré à VH-001 (même capacité, Essence)
--     VH-001 : non utilisé     ← test négatif : ne doit PAS être sélectionné
-- =============================================================
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (3, '2026-03-15 12:00:00', 'SP4-S3A', 1);


-- =============================================================
-- SCÉNARIO 4 — R3 : isolation stricte par créneau horaire
-- =============================================================
-- Hôtel : Ibis (id=3, 8 km)
--
-- ─── Créneau 14h00 ─────────────────────────────────────────────
-- FFD : SP4-S4A (1) et SP4-S4B (1) ex-æquo.
--   ① SP4-S4A (1 pax, 14h00)
--       Best-Fit cap ≥ 1 → taille min = 2 → VH-004 (2E, unique véhicule 2 places).
--       → SP4-S4A → VH-004. Restant : 2 − 1 = 1.
--
--   ② SP4-S4B (1 pax, 14h00)
--       VH-004 partiel (1 restant), MÊME horaire 14h00 → COVOITURAGE (R1 + R4b).
--       → SP4-S4B → VH-004. (VH-004 maintenant plein.)
--
-- ─── Créneau 14h30 ─────────────────────────────────────────────
-- SP4-S4C (1 pax, 14h30) — HORAIRE DIFFÉRENT.
--   VH-004 est en course pour son trajet de 14h00 (retour estimé ≈ 15h02).
--   Même s'il était libre, la RÈGLE 3 interdit de mélanger des horaires différents.
--   → Nouveau véhicule. Best-Fit cap ≥ 1 → taille min = 4.
--     VH-001 (4E) et VH-006 (4D) disponibles
--     (VH-006 rentré de 12h00/Colbert ≈ 13h30 ✓).
--     Diesel → VH-006.
--   → SP4-S4C → VH-006 (seul, créneau 14h30).
--
-- ✅ Résultat attendu :
--     14h00 → VH-004 : SP4-S4A + SP4-S4B  → badge "🚌 2 clients"
--     14h30 → VH-006 : SP4-S4C seul        → "-" (R3 : horaires différents)
-- =============================================================
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (1, '2026-03-15 14:00:00', 'SP4-S4A', 3),   -- 14h00, Ibis → VH-004 (covoiturage)
    (1, '2026-03-15 14:00:00', 'SP4-S4B', 3),   -- 14h00, Ibis → VH-004 (covoiturage)
    (1, '2026-03-15 14:30:00', 'SP4-S4C', 3);   -- 14h30, Ibis → véhicule séparé (R3)


-- =============================================================
-- SCÉNARIO 5A — R1 + R4b + R5
--               Multi-stop : ordre de dépôt par distance aéroport
-- =============================================================
-- Horaire : 2026-03-15 17:00  (tous véhicules disponibles à cet horaire)
-- Deux réservations, hôtels DIFFÉRENTS, même horaire → multi-stop dans une seule voiture.
--
-- FFD sort : SP4-S5A (3 pax) → SP4-S5B (1 pax)
--   ① SP4-S5A (3 pax, Ibis 8 km)
--       Best-Fit cap ≥ 3 → taille min = 4 → VH-001(4E) et VH-006(4D) ex-æquo.
--       Diesel → VH-006. Restant : 4 − 3 = 1.
--
--   ② SP4-S5B (1 pax, Novotel 10 km)
--       VH-006 partiel (1 restant), même horaire 17h00 → COVOITURAGE multi-stop (R4b).
--       → SP4-S5B → VH-006.
--
-- Règle R5 — tri dans VH-006 par distance aéroport croissante :
--   Ibis    (8 km)  → ordreDepot = 1  ← déposé EN PREMIER
--   Novotel (10 km) → ordreDepot = 2
--
-- Itinéraire : Aéroport → Ibis (8 km) → Novotel (10 km) → retour Aéroport
--
-- ✅ Résultat attendu :
--     VH-006 : SP4-S5A (Ibis, ordreDepot=1) + SP4-S5B (Novotel, ordreDepot=2)
--     → badge "🚌 2 clients"  |  Ibis déposé en premier, Novotel ensuite
-- =============================================================
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (3, '2026-03-15 17:00:00', 'SP4-S5A', 3),   -- Ibis    ( 8 km) → ordreDepot = 1
    (1, '2026-03-15 17:00:00', 'SP4-S5B', 2);   -- Novotel (10 km) → ordreDepot = 2


-- =============================================================
-- SCÉNARIO 5B — R5-ALPHA
--               Tie-break alphabétique (distances égales)
-- =============================================================
-- Horaire : 2026-03-15 19:00
-- Gare (id=5, 8 km) et Ibis (id=3, 8 km) : MÊME distance depuis l'aéroport.
--
-- FFD sort : SP4-S5E (1) et SP4-S5F (1) ex-æquo.
--   ① 1er pax → Best-Fit cap ≥ 1 → taille min = 2 → VH-004 (2E). Restant : 1.
--   ② 2e  pax → VH-004 partiel, même horaire 19h00 → COVOITURAGE.
--
-- Règle R5 dans VH-004 — distance égale → tie-break ALPHABÉTIQUE :
--   "Gare" (G) → ordreDepot = 1  ← déposé EN PREMIER  (G < I)
--   "Ibis" (I) → ordreDepot = 2
--
-- Itinéraire : Aéroport → Gare (8 km) → Ibis (8 km) → retour Aéroport
-- (SP4-S5F déposé AVANT SP4-S5E malgré l'ordre d'insertion → c'est R5-ALPHA)
--
-- ✅ Résultat attendu :
--     VH-004 : SP4-S5F (Gare, ordreDepot=1) + SP4-S5E (Ibis, ordreDepot=2)
--     → "Gare" avant "Ibis" car même distance, ordre alphabétique
-- =============================================================
INSERT INTO Reservation (nb_passager, date_heure_arrivee, id_client, id_hotel) VALUES
    (1, '2026-03-15 19:00:00', 'SP4-S5E', 3),   -- Ibis (8 km) — "I" > "G" → ordreDepot = 2
    (1, '2026-03-15 19:00:00', 'SP4-S5F', 5);   -- Gare (8 km) — "G" < "I" → ordreDepot = 1


-- =============================================================
-- VÉRIFICATION
-- =============================================================

-- 1. Toutes les réservations de test, triées par créneau
SELECT
    id_reservation,
    id_client,
    nb_passager   AS pax,
    date_heure_arrivee,
    id_hotel
FROM Reservation
WHERE id_client LIKE 'SP4-%'
ORDER BY date_heure_arrivee, id_hotel, nb_passager DESC;

-- 2. Véhicules disponibles (dont VH-006 ajouté pour les tests)
SELECT
    reference,
    place,
    CASE type_carburant WHEN 'D' THEN 'Diesel' ELSE 'Essence' END AS carburant
FROM Vehicule
ORDER BY place, type_carburant DESC;  -- tri : plus compact d'abord, Diesel prioritaire

-- 3. Distances depuis l'aéroport triées selon l'ordre R5
--    (distance croissante puis alphabétique = ordre de dépôt attendu)
SELECT
    h.nom,
    d.valeur AS dist_km
FROM Distance d
JOIN Hotel h ON h.id_hotel = d.to_hotel
WHERE d.from_hotel = 0
ORDER BY d.valeur ASC, h.nom ASC;   -- ← ordre exact produit par la Règle R5

-- 4. Planifications générées (à exécuter APRÈS appel API /planifications?date=2026-03-15)
SELECT
    p.id_planification,
    v.reference                 AS vehicule,
    r.id_client,
    r.nb_passager               AS pax,
    h.nom                       AS hotel,
    p.date_heure_depart_aeroport,
    p.date_heure_retour_aeroport
FROM Planification p
JOIN Reservation r ON r.id_reservation = p.id_reservation
JOIN Hotel       h ON h.id_hotel       = r.id_hotel
JOIN Vehicule    v ON v.id             = p.id_vehicule
WHERE r.id_client LIKE 'SP4-%'
ORDER BY p.date_heure_depart_aeroport, v.reference, r.nb_passager DESC;
