package com.hotel.model;

public class Hotel {
    private int id_hotel;
    private String nom;

    public Hotel() {
    }

    public Hotel(int id_hotel, String nom) {
        this.id_hotel = id_hotel;
        this.nom = nom;
    }

    public int getId_hotel() {
        return id_hotel;
    }

    public void setId_hotel(int id_hotel) {
        this.id_hotel = id_hotel;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
