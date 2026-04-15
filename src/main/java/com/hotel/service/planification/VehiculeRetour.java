package com.hotel.service.planification;

import java.sql.Timestamp;

import com.hotel.model.Vehicule;

/**
 * Encapsule un véhicule actuellement en trajet avec sa date de retour prévue.
 *
 * Cette classe est utilisée par l'algorithme Sprint 7 pour considérer les
 * véhicules
 * qui ne sont pas disponibles immédiatement mais qui le seront bientôt (après
 * leur retour).
 *
 * Exemple d'utilisation :
 * - Un véhicule V1 est parti à 10h00 et reviendra à 11h00
 * - Si un groupe doit partir à 10h30, V1 n'est pas disponible
 * - Mais si V1 est le meilleur choix (Best-Fit), on peut décider d'attendre son
 * retour
 * - La date de départ du groupe devient alors 11h00 (date de retour de V1)
 */
public class VehiculeRetour {

    /** Le véhicule en cours de trajet */
    private Vehicule vehicule;

    /**
     * Date/heure à laquelle le véhicule sera de nouveau disponible (retour à
     * l'aéroport)
     */
    private Timestamp dateRetour;

    /**
     * Constructeur par défaut.
     */
    public VehiculeRetour() {
    }

    /**
     * Constructeur avec paramètres.
     *
     * @param vehicule   le véhicule en cours de trajet
     * @param dateRetour la date/heure de retour prévue à l'aéroport
     */
    public VehiculeRetour(Vehicule vehicule, Timestamp dateRetour) {
        this.vehicule = vehicule;
        this.dateRetour = dateRetour;
    }

    /**
     * Retourne le véhicule encapsulé.
     *
     * @return le véhicule
     */
    public Vehicule getVehicule() {
        return vehicule;
    }

    /**
     * Définit le véhicule.
     *
     * @param vehicule le véhicule à définir
     */
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    /**
     * Retourne la date/heure de retour prévue.
     *
     * @return la date de retour à l'aéroport
     */
    public Timestamp getDateRetour() {
        return dateRetour;
    }

    /**
     * Définit la date/heure de retour.
     *
     * @param dateRetour la date de retour à définir
     */
    public void setDateRetour(Timestamp dateRetour) {
        this.dateRetour = dateRetour;
    }

    /**
     * Calcule le gaspillage (places vides) si ce véhicule est utilisé pour un
     * nombre de passagers donné.
     *
     * @param nbPassagers le nombre de passagers à transporter
     * @return le nombre de places qui resteraient vides (place - nbPassagers)
     */
    public int calculerGaspillage(int nbPassagers) {
        if (vehicule == null) {
            return Integer.MAX_VALUE;
        }
        return vehicule.getPlace() - nbPassagers;
    }

    /**
     * Vérifie si le véhicule a suffisamment de places pour les passagers demandés.
     *
     * @param nbPassagers le nombre de passagers à transporter
     * @return true si la capacité est suffisante, false sinon
     */
    public boolean peutAccueillir(int nbPassagers) {
        return vehicule != null && vehicule.getPlace() >= nbPassagers;
    }

    @Override
    public String toString() {
        return "VehiculeRetour{" +
                "vehicule=" + (vehicule != null ? vehicule.getReference() : "null") +
                ", dateRetour=" + dateRetour +
                '}';
    }
}
