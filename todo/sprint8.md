# Sprint 8 - Regroupement et Retour Véhicule

## Overview
Implémentation de la logique d'affectation des véhicules selon les regroupements et la gestion des retours de véhicule.

## Requis Métier

### Regroupement (DYNAMIQUE, non systématique)
- [ ] **Création regroupement**: Au premier vol, créer un regroupement

### Retour Véhicule - Logique
- [ ] **À la disponibilité d'un véhicule retourné**:
  - [ ] Récupérer immédiatement les réservations non assignées (resas reportées)
  - [ ] **Cas 1 - Véhicule rempli** (capacité atteinte): Départ direct
  - [ ] **Cas 2 - Véhicule incomplet**: Attendre `temps_attente` minutes
    - [ ] Si complété avant timeout: départ
    - [ ] Si timeout atteint: départ avec véhicule incomplet

### Regroupement Déclenché par Retour
- [ ] **Contrainte**: Ne doit PAS chevaucher un regroupement normal existant
- [ ] **Trigger**: Dès première disponibilité d'un véhicule
  - [ ] Vérifier s'il y a réservations non assignées
  - [ ] Vérifier pas chevaucher regroupement normal
  - [ ] Si ok: déclencher regroupement dynamique

### Priorité Réservations
- [ ] **Réservations non assignées** (reportées): **PRIORITAIRES**
  - [ ] Traitées en PREMIER quelle que soit la resa
  - [ ] Avant les réservations normales

## Implémentation - Logique Dynamique

### Vérification Chevaucher Regroupement
- [ ] Créer fonction `verifie_pas_chevaucher_regroupement(nouveau_regroupement, regroupements_normaux)`
- [ ] Retourner `true` si aucun chevauchement avec regroupement normal

### Trigger Regroupement Dynamique
- [ ] À chaque **retour de véhicule** (disponibilité):
  1. [ ] Récupérer réservations non assignées
  2. [ ] Si existe ET pas chevaucher:
     - [ ] Déclencher regroupement dynamique
  3. [ ] Appliquer planification (voir ci-dessous)

## Implémentation - Planification

- [ ] **1. Tri des regroupements** par date
- [ ] **2. Pour chaque regroupement**: Traiter par étapes

  ### Étape 1: Réservations non assignées (PRIORITAIRE)
  - [ ] Trier réservations non assignées/reportées (décroissant)
  - [ ] Pour chaque réservation:
    - [ ] Sélectionner **véhicule optimal**
    - [ ] Assigner au véhicule
    - [ ] **Cas voiture incomplète**: attendre `temps_attente` minutes
    - [ ] **Cas voiture complète**: départ direct

  ### Étape 2: Réservations normales (après non assignées)
  - [ ] Trier par nombre de clients (décroissant)
  - [ ] Pour chaque client:
    - [ ] Sélectionner **véhicule optimal**
    - [ ] **Cas voiture incomplète**: chercher réservations proches pour complétude
    - [ ] **Cas voiture complète**: compléter with véhicule optimal du client

  ### Étape 3: Clients/resas non assignés
  - [ ] Traiter les clients/resas non assignés au prochain regroupement (ou attendre)

## Implémentation - Véhicule Optimal

- [ ] **Liste véhicules disponibles** pour un regroupement:
  - [ ] Selon base de données
  - [ ] Selon fiverenany (disponibilité réelle)

- [ ] **Critères de sélection** (priorité):
  1. Capacité la plus proche de la réservation
  2. Si égal: moins de trajets
  3. Si égal: type carburant
  4. Sinon: choix aléatoire

## Code

### Backend - Services
- [ ] `RegroupementService`: créer/gérer regroupements normaux et dynamiques
- [ ] `RetourVehiculeHandler`: gérer logique retour véhicule
  - [ ] Récupérer réservations non assignées
  - [ ] Vérifier pas chevauchement
  - [ ] Déclencher regroupement si applicable
  - [ ] **Timeout manager**: attendre `temps_attente` minutes avant départ si incomplet
- [ ] `PlanificationService`: appliquer logique de planification
  - [ ] Séparer priorité réservations non assignées vs normales
  - [ ] Gérer queue d'attente pour timeout
- [ ] `VehiculeOptimalService`: sélectionner véhicule optimal
- [ ] `ChevauchementService`: vérifier pas chevaucher regroupements normaux

### Backend - Database
- [ ] Ajouter champs nécessaires pour regroupement dans modèle
- [ ] Ajouter logique de gestion date intervalle regroupement
- [ ] Ajouter logique de détection chevauchement regroupement

### Frontend - UI
- [ ] Affichage des regroupements
- [ ] Affichage de l'état d'assignation (assigné/non assigné)
- [ ] Gestion visuelle des retours de véhicule

## Testing

- [ ] **Test retour véhicule**:
  - [ ] Test véhicule rempli: départ immédiat
  - [ ] Test véhicule incomplet: attente `temps_attente` minutes
    - [ ] Si complété avant timeout: départ
    - [ ] Si timeout atteint: départ incomplet

- [ ] **Test chevauchement regroupement**:
  - [ ] Retour véhicule pendant regroupement normal: ne crée pas regroupement dynamique
  - [ ] Retour véhicule hors regroupement normal: crée regroupement dynamique

- [ ] **Test priorité réservations non assignées**:
  - [ ] Resas reportées traitées AVANT resas normales
  - [ ] Même regroupement: non assignées en premier

- [ ] **Test regroupement dynamique**:
  - [ ] Création correcte selon trigger (retour véhicule)
  - [ ] Pas de chevauchement avec regroupement normal
  - [ ] Assignation correcte des resas

- [ ] **Test véhicule optimal**: tous les critères de sélection
- [ ] **Edge cases**:
  - [ ] Réservations chevauchantes
  - [ ] Capacités limites
  - [ ] Pas de véhicule disponible
  - [ ] Timeout avant départ

## Checklist Finale

- [ ] Tous les tests passent
- [ ] Code commité sur branche `sprint-8-regroupement-si-retour-vehicule`
- [ ] Code review complétée
- [ ] PR créée vers `main`
- [ ] Documentation mise à jour
