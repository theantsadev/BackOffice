# Sprint 7 - Algorithme Best-Fit avec considération du retour véhicule

## Objectif

Optimiser l'assignation des réservations aux véhicules en utilisant une stratégie **Best-Fit** :
- Trouver le véhicule dont la capacité est la plus proche du nombre de passagers
- Remplir un véhicule au maximum avant d'en ouvrir un autre
- Prendre en compte les véhicules en cours de "retour" (indisponibles maintenant mais bientôt libres)

---

## Règles métier

### 1. Tri initial du groupe
Les réservations d'un groupe sont triées par **nb_passagers décroissant**.
> Objectif : assigner d'abord les grosses réservations pour éviter le gaspillage de places.

### 2. Sélection du véhicule (Best-Fit)
Pour chaque réservation, on cherche le véhicule **le plus adapté** :

```
vehicule_optimal = MIN( place - nb_passagers )
                   où place >= nb_passagers
```

- On cherche le véhicule avec `place >= nb_passagers` **ET** `|place - nb_passagers|` minimal
- Si aucun véhicule n'a assez de places → **scinder** la réservation sur plusieurs véhicules

### 3. Remplissage prioritaire du véhicule en cours
Une fois un véhicule assigné à la première réservation du groupe :
- On essaie de **remplir ce véhicule d'abord** avec les réservations suivantes
- Critère de sélection : `|nb_pax - places_restantes|` **minimal**
- On ne passe à un nouveau véhicule que si le véhicule courant est plein

### 4. Prise en compte du retour véhicule
Un véhicule peut être **en cours de trajet** (indisponible maintenant) mais **bientôt disponible** :

```
Si vehicule.date_heure_retour_aeroport > date_heure_depart_groupe
   ET vehicule est le plus approprié (Best-Fit)
   ALORS date_heure_depart_groupe = vehicule.date_heure_retour_aeroport
```

> Le groupe attend que le véhicule revienne plutôt que de prendre un véhicule moins optimal.

---

## Exemple fonctionnel

### Données initiales

**Réservations (groupe trié décroissant) :**
| id_resa | nb_passagers |
|---------|--------------|
| R1      | 6            |
| R2      | 4            |
| R3      | 3            |
| R4      | 2            |

**Véhicules disponibles :**
| id_vehicule | places | statut                          |
|-------------|--------|----------------------------------|
| V1          | 7      | Disponible                       |
| V2          | 4      | En retour (retour dans 15 min)   |
| V3          | 9      | Disponible                       |

### Déroulement de l'algorithme

**Étape 1 : R1 (6 pax)**
- Best-Fit : V1 (7 places) → gaspillage = 1 place
- V3 (9 places) → gaspillage = 3 places
- **Choix : V1** (gaspillage minimal)
- V1 : 7 - 6 = **1 place restante**

**Étape 2 : R2 (4 pax)**
- V1 a 1 place restante (insuffisant)
- Best-Fit parmi véhicules libres : V2 (4 places, en retour) → gaspillage = 0
- V3 (9 places) → gaspillage = 5
- **Choix : V2** (Best-Fit parfait)
- **Date départ groupe = date retour V2** (on attend V2)
- V2 : 4 - 4 = **0 places restantes**

**Étape 3 : R3 (3 pax)**
- V1 : 1 place (insuffisant)
- V2 : 0 places (plein)
- **Choix : V3** (seul disponible)
- V3 : 9 - 3 = **6 places restantes**

**Étape 4 : R4 (2 pax)**
- V3 a 6 places restantes
- Remplissage prioritaire : `|2 - 6| = 4`
- **Choix : V3** (on remplit le véhicule déjà ouvert)
- V3 : 6 - 2 = **4 places restantes**

### Résultat final
| Véhicule | Réservations | Passagers | Places vides |
|----------|--------------|-----------|--------------|
| V1       | R1           | 6         | 1            |
| V2       | R2           | 4         | 0            |
| V3       | R3, R4       | 5         | 4            |

---

## Implémentation technique

### Modifications dans `VehiculeSelectionService.java`

#### Nouvelle méthode : `trouverVehiculeBestFit`
```java
/**
 * Trouve le véhicule avec le moins de gaspillage (Best-Fit).
 *
 * @param nbPax nombre de passagers à assigner
 * @param depart date/heure de départ souhaitée
 * @param retour date/heure de retour estimée
 * @param vehiculesExclus IDs des véhicules déjà utilisés dans ce groupe
 * @return le véhicule optimal ou null si aucun disponible
 */
public Vehicule trouverVehiculeBestFit(int nbPax, Timestamp depart, Timestamp retour,
                                        List<Integer> vehiculesExclus) throws SQLException
```

#### Nouvelle méthode : `trouverVehiculeEnRetour`
```java
/**
 * Cherche un véhicule en cours de trajet qui sera bientôt disponible.
 *
 * @param nbPax nombre de passagers
 * @param departSouhaite date/heure de départ souhaitée
 * @param vehiculesExclus IDs à exclure
 * @return VehiculeRetour contenant le véhicule et sa date de retour, ou null
 */
public VehiculeRetour trouverVehiculeEnRetour(int nbPax, Timestamp departSouhaite,
                                               List<Integer> vehiculesExclus) throws SQLException
```

### Modifications dans `GroupeAssignationService.java`

#### Refactoring de `traiterGroupe`
```java
/**
 * Traite un groupe avec l'algorithme Best-Fit + considération retour véhicule.
 *
 * 1. Trie les réservations par nb_passagers DESC
 * 2. Pour chaque réservation :
 *    a. Cherche dans les bins existants (véhicules déjà ouverts)
 *    b. Si pas de place → cherche nouveau véhicule Best-Fit
 *    c. Si véhicule en retour est meilleur → attend son retour
 * 3. Remplit le véhicule courant avant d'en ouvrir un autre
 */
public List<GroupAssignment> traiterGroupe(Timestamp depart, Timestamp retour,
                                            List<Reservation> groupe,
                                            List<Reservation> nonAssignees) throws SQLException
```

#### Nouvelle méthode : `trouverMeilleurBinBestFit`
```java
/**
 * Trouve le bin (véhicule ouvert) qui minimise |places_restantes - nb_pax|.
 *
 * @param bins liste des véhicules déjà ouverts avec leurs places restantes
 * @param nbPassagers nombre de passagers à assigner
 * @return index du meilleur bin ou -1 si aucun ne convient
 */
public int trouverMeilleurBinBestFit(List<VehiculeBin> bins, int nbPassagers)
```

### Nouveau modèle : `VehiculeRetour.java`
```java
package com.hotel.service.planification;

import java.sql.Timestamp;
import com.hotel.model.Vehicule;

/**
 * Encapsule un véhicule en cours de retour avec sa date de disponibilité.
 */
public class VehiculeRetour {
    private Vehicule vehicule;
    private Timestamp dateRetour;  // Date à laquelle le véhicule sera disponible

    // Getters, setters, constructeur
}
```

---

## Requêtes SQL

### Véhicules en cours de retour
```sql
-- Trouver les véhicules qui seront disponibles après leur retour
SELECT v.*, p.date_heure_retour_aeroport as date_retour
FROM Vehicule v
JOIN Planification p ON p.id_vehicule = v.id
WHERE v.place >= ?                              -- Capacité suffisante
  AND p.date_heure_retour_aeroport > ?          -- Pas encore revenu
  AND p.date_heure_retour_aeroport <= ?         -- Revient dans un délai acceptable
  AND v.id NOT IN (?)                           -- Pas déjà utilisé
ORDER BY (v.place - ?) ASC                       -- Best-Fit : gaspillage minimal
LIMIT 1
```

---

## Fonctions existantes à réutiliser

| Fonction | Fichier | Utilisation |
|----------|---------|-------------|
| `estVoitureDisponible` | VehiculeSelectionService | Vérifier disponibilité |
| `chargerBinsExistants` | GroupeAssignationService | Récupérer véhicules ouverts |
| `assignerSurBinExistant` | GroupeAssignationService | Assigner sur véhicule ouvert |
| `assignerSurNouveauVehicule` | GroupeAssignationService | Ouvrir nouveau véhicule |
| `copierReservationAvecNbPassagers` | GroupeAssignationService | Scinder une réservation |
| `persisterPlanificationsGroupe` | GroupeAssignationService | Sauvegarder en base |

---

## Workflow de développement

### Branche
```bash
git checkout -b sprint-7-best-fit-retour
```

### Commits suggérés
```bash
git commit -m "feat: add VehiculeRetour model for tracking returning vehicles"
git commit -m "feat: add trouverVehiculeBestFit in VehiculeSelectionService"
git commit -m "feat: add trouverVehiculeEnRetour to handle waiting for vehicle return"
git commit -m "feat: refactor trouverMeilleurBin to use Best-Fit strategy"
git commit -m "feat: update traiterGroupe to consider vehicle return dates"
git commit -m "test: add unit tests for Best-Fit algorithm"
```

---

## Validation

### Cas de test

1. **Best-Fit basique** : Véhicule 7 places vs 9 places pour 6 passagers → choisit 7 places
2. **Remplissage prioritaire** : 2 résas (4 pax + 3 pax), véhicule 8 places → les 2 dans le même véhicule
3. **Attente retour** : Véhicule parfait en retour dans 10 min vs véhicule dispo avec gaspillage → attend le retour
4. **Scission** : 10 passagers, véhicules 7 et 4 places → split sur les 2 véhicules

### Checklist
- [ ] Les réservations sont triées par nb_passagers DESC
- [ ] Le véhicule Best-Fit (gaspillage minimal) est sélectionné
- [ ] Un véhicule est rempli au maximum avant d'en ouvrir un autre
- [ ] Les véhicules en retour sont considérés si plus optimaux
- [ ] La date de départ groupe s'ajuste quand on attend un véhicule
- [ ] La scission fonctionne si aucun véhicule n'a assez de places
