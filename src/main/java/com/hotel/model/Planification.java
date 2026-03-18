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
    private int nbPassager;
    private int idHotel;
    private String nomHotel;
    private String referenceVehicule;
    private double distanceAeroport; // distance aéroport → hôtel en km
    private double distanceTotaleTrajet; // distance totale du trajet véhicule (aller + retour)
    private double distanceSegmentKm; // distance depuis l'arrêt précédent
    private double distanceProgressiveKm; // distance cumulée depuis l'aéroport
    private int ordreAssignGroupe; // ordre d'assignation dans le groupe de depart
    private int ordreAssignGlobal; // ordre d'assignation global sur la journee
    private int ordreDepot;          // ordre de dépôt : 1 = plus proche de l'aéroport

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

    public int getNbPassager() {
        return nbPassager;
    }

    public void setNbPassager(int nbPassager) {
        this.nbPassager = nbPassager;
    }

    public String getNomHotel() {
        return nomHotel;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
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

    public double getDistanceAeroport() {
        return distanceAeroport;
    }

    public void setDistanceAeroport(double distanceAeroport) {
        this.distanceAeroport = distanceAeroport;
    }

    public double getDistanceTotaleTrajet() {
        return distanceTotaleTrajet;
    }

    public void setDistanceTotaleTrajet(double distanceTotaleTrajet) {
        this.distanceTotaleTrajet = distanceTotaleTrajet;
    }

    public double getDistanceSegmentKm() {
        return distanceSegmentKm;
    }

    public void setDistanceSegmentKm(double distanceSegmentKm) {
        this.distanceSegmentKm = distanceSegmentKm;
    }

    public double getDistanceProgressiveKm() {
        return distanceProgressiveKm;
    }

    public void setDistanceProgressiveKm(double distanceProgressiveKm) {
        this.distanceProgressiveKm = distanceProgressiveKm;
    }

    public int getOrdreAssignGroupe() {
        return ordreAssignGroupe;
    }

    public void setOrdreAssignGroupe(int ordreAssignGroupe) {
        this.ordreAssignGroupe = ordreAssignGroupe;
    }

    public int getOrdreAssignGlobal() {
        return ordreAssignGlobal;
    }

    public void setOrdreAssignGlobal(int ordreAssignGlobal) {
        this.ordreAssignGlobal = ordreAssignGlobal;
    }

    public int getOrdreDepot() {
        return ordreDepot;
    }

    public void setOrdreDepot(int ordreDepot) {
        this.ordreDepot = ordreDepot;
    }
}
