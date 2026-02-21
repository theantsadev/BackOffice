# Sprint 3

**TL** : Vicky , 3254  
**Dev1** : Jordi , 3351 -> FO  
**Dev2** : mike , 3327 -> BO

---

## Scripts

- Script d'insertion des tables (DDL)
- Script d'insertion des données initiales (hotels, distances, paramètres)
- Script de réinitialisation (supprime toutes les données sans supprimer les tables)

---

## Tables

### Parametre *(accès par base uniquement, pas de CRUD)*
- id_parametre SERIAL (PK)
- cle VARCHAR UNIQUE
- valeur DOUBLE
- unite VARCHAR

**Données initiales :**
- `vitesse_moyenne_kmh` = 30
- `temps_attente_min` = 30

### Distance
- id_distance SERIAL (PK)
- from_hotel INTEGER (FK -> Hotel)
- to_hotel INTEGER (FK -> Hotel)
- valeur DOUBLE (en km)

> ⚠️ La distance A→B = B→A. On n'insère qu'une seule direction en base (from < to par convention). La requête doit chercher dans les deux sens.

### Planification
- id_planification SERIAL (PK)
- id_reservation INTEGER (FK -> Reservation)
- id_vehicule INTEGER (FK -> Vehicule)
- date_heure_depart_aeroport DATETIME
- date_heure_retour_aeroport DATETIME

---

## Règles de gestion *(Sprint 3 : un lieu par réservation, sans regroupement ni temps d'attente)*

**Sélection du véhicule approprié pour une réservation :**

1. `nb_passager` de la réservation <= `place` du véhicule
2. Parmi les véhicules valides, choisir celui qui laisse le moins de places vides *(espace vide = place - nb_passager)*
3. Si égalité de capacité disponible → priorité au **Diesel** (`type_carburant = 'D'`)
4. Si égalité de capacité ET de type → **random**
5. Le véhicule doit être **disponible** sur le créneau de la planification

**Calcul des créneaux :**

- `duree_trajet` = `distance(aeroport, hotel)` / `vitesse_moyenne` *(en heures)*
- `date_heure_depart_aeroport` = `date_heure_arrivee_vol` - `duree_trajet`
- `date_heure_retour_aeroport` = `date_heure_arrivee_vol` + `duree_trajet`

**Disponibilité d'un véhicule :**
Un véhicule est disponible si aucune planification existante ne chevauche le créneau `[depart, retour]` demandé.

---

## BackOffice

### Affichage

**formulaire-planification.jsp**
- Champ : Date de planification `[Sélecteur de date]`
- Bouton `[Rechercher]` → appel métier `getPlanificationsByDate(Date date)` → redirige vers `liste-planification.jsp`

**liste-planification.jsp** *(sous forme de tableau)*

| id_planification | id_reservation | id_client | hotel | id_vehicule | Départ aéroport | Retour aéroport |
|---|---|---|---|---|---|---|

**liste-reservation-non-assignees.jsp** *(sous forme de tableau)*

| id_reservation | id_client | nb_passager | date_heure_arrivee | hotel | Action |
|---|---|---|---|---|---|
| ... | ... | ... | ... | ... | [Assigner] |

> Le bouton `[Assigner]` appelle `getVehiculeApproprieForReservation(id_reservation)` puis `planifier(...)` automatiquement.

---

## Métier

### Classe Parametre
```java
Parametre getParametreByCle(String cle)
```

### Classe Distance
```java
// Cherche dans les deux sens (from/to ou to/from)
Distance getDistanceByFromAndTo(int fromHotelId, int toHotelId)
```

### Classe Planification
```java
// Récupère toutes les planifications d'une date donnée
List<Planification> getPlanificationsByDate(Date date)

// Récupère les réservations sans planification associée
List<Reservation> getReservationsNonAssignees()

// Vérifie si un véhicule est libre sur un créneau donné
// ✅ Corrigé : nécessite l'id du véhicule ET le créneau
Boolean estVoitureDisponible(int id_vehicule, Datetime dateHeureDepart, Datetime dateHeureRetour)

// Calcule la durée du trajet aéroport <-> hotel (en minutes)
double getDureeTrajetMinutes(int id_reservation)

// Calcule l'heure de départ depuis l'aéroport (avant l'arrivée du vol)
Datetime getDateHeureDepartAeroport(int id_reservation)

// Calcule l'heure de retour à l'aéroport (après dépôt au hotel)
Datetime getDateHeureRetourAeroport(int id_reservation)

// Sélectionne le véhicule le plus approprié selon les règles de gestion
// Retourne null si aucun véhicule disponible
Vehicule getVehiculeApproprieForReservation(int id_reservation)

// Crée une planification en base
Planification planifier(int id_reservation, int id_vehicule, Datetime dateHeureDepart, Datetime dateHeureRetour)
```

---

## API REST *(consommée par le FO)*

```
GET  /planifications?date=YYYY-MM-DD   → getPlanificationsByDate(Date date)
GET  /reservations/non-assignees       → getReservationsNonAssignees()
POST /planifications                   → planifier(id_reservation, id_vehicule, date_heure_depart, date_heure_retour)
```

> Le FO doit envoyer le token dans le header (comme Sprint 2) pour accéder aux endpoints.

---

## Points à ne PAS implémenter en Sprint 3 *(reportés)*
- Regroupement de réservations (temps d'attente entre vols)
- Gestion multi-lieux (un seul hotel par réservation pour l'instant)

---

## Workflow GitHub

> Tout le développement Sprint 3 se fait dans le repo **BO** (même base, même projet).

### Branches

```
main
└── sprint-3-feat1       (Vicky — TL — Scripts DB)
└── sprint-3-feat2       (mike — Dev2 — Métier + API + JSP planification)
└── sprint-3-feat3       (Jordi — Dev1 — JSP réservations non assignées)
```

> Chaque dev crée sa branche depuis `main` en début de sprint. Les merges vers `main` se font via **Pull Request**, validée par le TL (Vicky).

---

### sprint-3-script-bdd *(Vicky — Scripts DB)*

```bash
git checkout -b sprint-3-script-bdd

git commit -m "feat: create DDL script for Parametre, Distance, Planification tables"
git commit -m "feat: add initial data script (hotels, distances, parametres)"
git commit -m "feat: add reset script to clear all data"
```

---

### sprint-3-metier-planification *(mike — Métier + API + JSP planification)*

```bash
git checkout -b sprint-3-metier-planification

# Métier
git commit -m "feat: add getParametreByCle in Parametre class"
git commit -m "feat: add getDistanceByFromAndTo with bidirectional lookup"
git commit -m "feat: add estVoitureDisponible(id_vehicule, depart, retour)"
git commit -m "feat: add getDureeTrajetMinutes and calcul depart/retour aeroport"
git commit -m "feat: add getVehiculeApproprieForReservation with selection rules"
git commit -m "feat: add planifier method - insert Planification in DB"
git commit -m "feat: add getPlanificationsByDate in Planification class"

# API REST
git commit -m "feat: add GET /planifications?date endpoint"
git commit -m "feat: add POST /planifications endpoint"

# JSP
git commit -m "feat: add formulaire-planification.jsp with date picker"
git commit -m "feat: add liste-planification.jsp with planification table"
```

---

### sprint-3-reservations-non-assigne *(Jordi — JSP réservations non assignées)*

```bash
git checkout -b sprint-3-feat3

git commit -m "feat: add getReservationsNonAssignees in Planification class"
git commit -m "feat: add GET /reservations/non-assignees endpoint"
git commit -m "feat: add liste-reservation-non-assignees.jsp with assign button"
```

---

### Merge & Pull Request

```bash
# Une fois la branche prête :
git push origin sprint-3-feat1   # ou feat2 / feat3

# Sur GitHub :
# 1. Créer une Pull Request vers main
# 2. TL (Vicky) review et approuve
# 3. Merge après validation
# 4. Supprimer la branche mergée
```

> **Ordre recommandé des merges :**
> 1. `sprint-3-feat1` en premier (les tables doivent exister avant les tests)
> 2. `sprint-3-feat2` et `sprint-3-feat3` peuvent merger en parallèle ensuite