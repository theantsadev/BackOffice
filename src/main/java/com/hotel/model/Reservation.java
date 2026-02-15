package com.hotel.model;

import java.sql.Timestamp;

public class Reservation {
    private int id_reservation;
    private String id_client;
    private int nb_passager;
    private Timestamp date_heure_arrivee;
    private int id_hotel;
    private String nom_hotel;

    public Reservation() {
    }

    public Reservation(int id_reservation, String id_client, int nb_passager,
            Timestamp date_heure_arrivee, int id_hotel) {
        this.id_reservation = id_reservation;
        this.id_client = id_client;
        this.nb_passager = nb_passager;
        this.date_heure_arrivee = date_heure_arrivee;
        this.id_hotel = id_hotel;
    }

    public int getId_reservation() {
        return id_reservation;
    }

    public void setId_reservation(int id_reservation) {
        this.id_reservation = id_reservation;
    }

    public String getId_client() {
        return id_client;
    }

    public void setId_client(String id_client) {
        this.id_client = id_client;
    }

    public int getNb_passager() {
        return nb_passager;
    }

    public void setNb_passager(int nb_passager) {
        this.nb_passager = nb_passager;
    }

    public Timestamp getDate_heure_arrivee() {
        return date_heure_arrivee;
    }

    public void setDate_heure_arrivee(Timestamp date_heure_arrivee) {
        this.date_heure_arrivee = date_heure_arrivee;
    }

    public int getId_hotel() {
        return id_hotel;
    }

    public void setId_hotel(int id_hotel) {
        this.id_hotel = id_hotel;
    }

    public String getNom_hotel() {
        return nom_hotel;
    }

    public void setNom_hotel(String nom_hotel) {
        this.nom_hotel = nom_hotel;
    }
}
