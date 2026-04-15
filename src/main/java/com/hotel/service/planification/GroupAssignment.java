package com.hotel.service.planification;

import java.sql.Timestamp;

import com.hotel.model.Reservation;

/**
 * Représente une assignation d'une réservation (ou partie de réservation) à un véhicule.
 *
 * Sprint 7 : Ajout de dateDepart pour gérer les cas où différents véhicules
 * ont des heures de disponibilité différentes (heure_debut_dispo).
 */
public class GroupAssignment {
    private final Reservation reservation;
    private final int idVehicule;
    private final int nbPassagersAssignes;
    private final Timestamp dateDepart; // Date/heure de départ effective pour ce véhicule

    /**
     * Constructeur avec date de départ explicite.
     * Utilisé quand le véhicule a une heure de disponibilité spécifique.
     */
    public GroupAssignment(Reservation reservation, int idVehicule, int nbPassagersAssignes, Timestamp dateDepart) {
        this.reservation = reservation;
        this.idVehicule = idVehicule;
        this.nbPassagersAssignes = nbPassagersAssignes;
        this.dateDepart = dateDepart;
    }

    /**
     * Constructeur sans date de départ (rétrocompatibilité).
     * La date sera déterminée ailleurs (fallback sur l'heure d'arrivée).
     */
    public GroupAssignment(Reservation reservation, int idVehicule, int nbPassagersAssignes) {
        this(reservation, idVehicule, nbPassagersAssignes, null);
    }

    public Reservation getReservation() {
        return reservation;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public int getNbPassagersAssignes() {
        return nbPassagersAssignes;
    }

    public Timestamp getDateDepart() {
        return dateDepart;
    }
}
