package com.hotel.model;

import java.sql.Timestamp;

public class Planification {
    private int idPlanification;
    private int idReservation;
    private int idVehicule;
    private Timestamp dateHeureDepartAeroport;
    private Timestamp dateHeureRetourAeroport;

    // Champs additionnels pour l'affichage (jointures)
    private String idClient;
    private String nomHotel;
    private String referenceVehicule;

    public Planification() {
    }

    public Planification(int idPlanification, int idReservation, int idVehicule,
                         Timestamp dateHeureDepartAeroport, Timestamp dateHeureRetourAeroport) {
        this.idPlanification = idPlanification;
        this.idReservation = idReservation;
        this.idVehicule = idVehicule;
        this.dateHeureDepartAeroport = dateHeureDepartAeroport;
        this.dateHeureRetourAeroport = dateHeureRetourAeroport;
    }

    public int getIdPlanification() {
        return idPlanification;
    }

    public void setIdPlanification(int idPlanification) {
        this.idPlanification = idPlanification;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public Timestamp getDateHeureDepartAeroport() {
        return dateHeureDepartAeroport;
    }

    public void setDateHeureDepartAeroport(Timestamp dateHeureDepartAeroport) {
        this.dateHeureDepartAeroport = dateHeureDepartAeroport;
    }

    public Timestamp getDateHeureRetourAeroport() {
        return dateHeureRetourAeroport;
    }

    public void setDateHeureRetourAeroport(Timestamp dateHeureRetourAeroport) {
        this.dateHeureRetourAeroport = dateHeureRetourAeroport;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getNomHotel() {
        return nomHotel;
    }

    public void setNomHotel(String nomHotel) {
        this.nomHotel = nomHotel;
    }

    public String getReferenceVehicule() {
        return referenceVehicule;
    }

    public void setReferenceVehicule(String referenceVehicule) {
        this.referenceVehicule = referenceVehicule;
    }
}
