# Sprint 4 — F2 : Ordre de dépôt automatique par distance

## Objectif
Lors d'une planification, déterminer automatiquement l'ordre dans lequel les clients sont déposés, en partant de l'hôtel le plus proche de l'aéroport vers le plus éloigné.

---

## Règles appliquées

1. **Distance croissante** — le client dont l'hôtel est le plus proche de l'aéroport est déposé en premier (ordre 1).
2. **Égalité de distance → ordre alphabétique** sur le nom de l'hôtel.
3. **Distance inconnue** (absente de la table `Distance`) → client placé en dernier.

**Exemple :**
- Hôtel A à 3 km, Hôtel B à 7 km, Hôtel C à 3 km (al. = C > A)
- Ordre : Hôtel A (1) → Hôtel C (2) → Hôtel B (3)

---

## Implémentation

### `Planification.java`
- Champ `distanceAeroport` (double, km) — distance aéroport → hôtel du client
- Champ `ordreDepot` (int) — rang de dépôt dans le trajet (commence à 1)

### `PlanificationService.java` — `getPlanificationsByDate`
- **SQL** : `LEFT JOIN Distance d ON d.from_hotel = 0 AND d.to_hotel = r.id_hotel`  
  (l'aéroport est stocké avec `id_hotel = 0` dans la table Distance)
- **Post-traitement** : regroupement par `(idVehicule, dateDepart, dateRetour)`, tri par `distanceAeroport ASC` puis `nomHotel ASC`, affectation séquentielle de `ordreDepot`

### `liste-planification.jsp`
- Colonne **"Ordre Dépôt"** dans le tableau principal (badge circulaire coloré)
- Section **"Itinéraires de Dépôt par Véhicule"** en bas de page :
  - Une carte par véhicule/créneau
  - Départ ✈ Aéroport puis arrêts numérotés : hôtel, client, passagers, distance

---

## Fichiers modifiés

| Fichier | Modification |
|---|---|
| `Planification.java` | `+distanceAeroport`, `+ordreDepot` |
| `PlanificationService.java` | JOIN Distance + tri + calcul ordreDepot |
| `liste-planification.jsp` | Colonne ordre + section itinéraires |
