# Sprint 7 : Fractionnement des passagers sur plusieurs véhicules

## Objectif
Permettre à la planification automatique de répartir les passagers d'une même réservation sur plusieurs véhicules quand un seul véhicule ne peut pas tout absorber.

Si tous les passagers ne peuvent pas être transportés dans le groupe courant (même fenêtre d'attente), le reliquat est automatiquement reporté au prochain groupe d'attente.

---

## Règles métier ajoutées

1. Une réservation peut être planifiée en plusieurs lignes de planification (split).
2. Chaque ligne stocke le nombre de passagers réellement affectés au véhicule.
3. Le calcul des réservations non assignées est fait sur le reste à transporter :
   - reste = nb_passager_reservation - somme(passagers_assignes)
4. Si reste > 0, la réservation réapparaît dans les non assignées avec ce reste.
5. Le reliquat est réinjecté dans le prochain groupe d'attente lors de la planification auto.

---

## Exemple fonctionnel

Données :
- cl_1 : 5 passagers
- cl_2 : 8 passagers
- v_1 : 7 places
- v_2 : 4 places

Résultat possible :
- v_1 : 5 passagers de cl_1 + 2 passagers de cl_2
- v_2 : 4 passagers de cl_2
- reliquat cl_2 : 2 passagers non assignés
- ces 2 passagers seront proposés au prochain temps d'attente

---

## Implémentation technique

### PlanificationService.java

Principaux changements :
- Surcharge de la méthode de planification pour accepter nbPassagersAssignes.
- Insertion en base avec le champ nb_passager_assigne.
- Algorithme auto mis à jour pour traiter une réservation avec un compteur passagersRestants.
- Affectation partielle possible sur un bin existant (véhicule déjà ouvert) ou un nouveau véhicule.
- Si passagersRestants > 0 en fin de traitement, création d'une réservation partielle non assignée pour le prochain groupe.

Requêtes mises à jour :
- Lecture des planifications : utilisation de COALESCE(nb_passager_assigne, nb_passager) pour compatibilité des anciennes lignes.
- Calcul des non assignées : sélection basée sur le reliquat restant et non plus uniquement sur l'absence totale de planification.
- Calcul des places prises par véhicule : somme de nb_passager_assigne (avec fallback).

### liste-planification.jsp

- Suppression d'un dédoublonnage par idReservation dans l'affichage des trajets.
- Les sous-assignations d'une même réservation sont maintenant visibles dans le récapitulatif.

---

## Changement base de données

### Nouvelle migration
- V10__add_nb_passager_assigne_to_planification.sql
  - Ajoute la colonne nb_passager_assigne dans Planification.

### Script reset
- reset.sql mis à jour pour inclure la colonne nb_passager_assigne.

---

## Compatibilité

La logique reste compatible avec les anciennes données :
- si nb_passager_assigne est null sur une ancienne ligne, le système retombe sur nb_passager de la réservation.

---

## Fichiers modifiés

| Fichier | Modification |
|---|---|
| src/main/java/com/hotel/service/PlanificationService.java | Support du split passagers, calcul reliquat, nouvelles requêtes SQL |
| src/main/webapp/pages/liste-planification.jsp | Affichage des lignes fractionnées dans les trajets |
| src/main/resources/db/reset.sql | Ajout de la colonne nb_passager_assigne |
| src/main/resources/db/V10__add_nb_passager_assigne_to_planification.sql | Migration SQL Sprint 7 |

---

## Validation

- Compilation Maven effectuée avec succès.
- Cas métier visé : fractionnement d'une même réservation sur plusieurs véhicules + report du reliquat au prochain groupe.
