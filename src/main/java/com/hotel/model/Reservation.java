package com.hotel.model;

import java.sql.Timestamp;

public class Reservation {
    private int id_reservation;
    private String id_client;
    private int nb_passager;
    private Timestamp date_heure_arrivee;
    private Timestamp date_heure_depart_groupe;
    private int id_hotel;
    private String nom_hotel;
    private int ordre_assign_groupe;
    private int ordre_assign_global;

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

    public Timestamp getDate_heure_depart_groupe() {
        return date_heure_depart_groupe;
    }

    public void setDate_heure_depart_groupe(Timestamp date_heure_depart_groupe) {
        this.date_heure_depart_groupe = date_heure_depart_groupe;
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

    public int getOrdre_assign_groupe() {
        return ordre_assign_groupe;
    }

    public void setOrdre_assign_groupe(int ordre_assign_groupe) {
        this.ordre_assign_groupe = ordre_assign_groupe;
    }

    public int getOrdre_assign_global() {
        return ordre_assign_global;
    }

    public void setOrdre_assign_global(int ordre_assign_global) {
        this.ordre_assign_global = ordre_assign_global;
    }
}
