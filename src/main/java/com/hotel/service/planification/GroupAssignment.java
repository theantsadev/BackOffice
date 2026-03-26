package com.hotel.service.planification;

import com.hotel.model.Reservation;

public class GroupAssignment {
    private final Reservation reservation;
    private final int idVehicule;
    private final int nbPassagersAssignes;

    public GroupAssignment(Reservation reservation, int idVehicule, int nbPassagersAssignes) {
        this.reservation = reservation;
        this.idVehicule = idVehicule;
        this.nbPassagersAssignes = nbPassagersAssignes;
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
}
