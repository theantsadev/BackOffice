# Sprint 4 — F1 : Covoiturage automatique

## Objectif
Permettre à plusieurs clients d'être assignés dans un même véhicule lors de la planification automatique, selon la capacité disponible.

**Exemple :**
- voiture_A (10 places), voiture_B (6 places), voiture_C (4 places)
- client_1 (7 pax) + client_2 (3 pax) → même heure, même hôtel
- Résultat : les deux clients sont placés dans voiture_A (7 + 3 = 10 places)

---

## Algorithme — First-Fit Decreasing (FFD)

1. **Groupement** : les réservations ayant la même `date_heure_arrivee` ET le même `id_hotel` sont regroupées (même trajet possible).
2. **Tri** : au sein de chaque groupe, les réservations sont triées par `nb_passager DESC` (plus grands groupes en premier).
3. **Remplissage** :
   - Pour chaque réservation, on cherche un véhicule déjà ouvert avec assez de places restantes (**best-fit**).
   - Si trouvé → covoiturage : on insère la planification dans ce véhicule.
   - Sinon → on ouvre un nouveau véhicule disponible.

---

## Fichiers modifiés

| Fichier | Modification |
|---|---|
| `PlanificationService.java` | Nouvelle méthode `trouverVehiculeDisponible`, refonte de `planifierAutoParDate` avec FFD |
| `Planification.java` | Ajout du champ `nbPassager` |
| `liste-planification.jsp` | Colonnes **Nb Passagers** et **Covoiturage** (badge bleu si plusieurs clients partagent un véhicule) |
