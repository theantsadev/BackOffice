-- =====================================================================
-- V11__add_heure_debut_dispo_to_vehicule.sql
-- =====================================================================
-- Flyway Migration : Ajoute la colonne heure_debut_dispo à la table Vehicule
-- Cette colonne est utilisée par Sprint 7 pour vérifier la disponibilité
-- des véhicules à une heure spécifique.

ALTER TABLE Vehicule
ADD COLUMN heure_debut_dispo TIME DEFAULT '00:00:00';

-- Mise à jour : les véhicules existants sont disponibles depuis minuit
UPDATE Vehicule SET heure_debut_dispo = '00:00:00' WHERE heure_debut_dispo IS NULL;
