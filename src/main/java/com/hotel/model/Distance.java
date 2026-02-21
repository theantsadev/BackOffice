package com.hotel.model;

public class Distance {
    private int idDistance;
    private int fromHotel;
    private int toHotel;
    private double valeur;

    public Distance() {
    }

    public Distance(int idDistance, int fromHotel, int toHotel, double valeur) {
        this.idDistance = idDistance;
        this.fromHotel = fromHotel;
        this.toHotel = toHotel;
        this.valeur = valeur;
    }

    public int getIdDistance() {
        return idDistance;
    }

    public void setIdDistance(int idDistance) {
        this.idDistance = idDistance;
    }

    public int getFromHotel() {
        return fromHotel;
    }

    public void setFromHotel(int fromHotel) {
        this.fromHotel = fromHotel;
    }

    public int getToHotel() {
        return toHotel;
    }

    public void setToHotel(int toHotel) {
        this.toHotel = toHotel;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }
}
