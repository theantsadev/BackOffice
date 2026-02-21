package com.hotel.model;

public class Parametre {
    private int idParametre;
    private String cle;
    private double valeur;
    private String unite;

    public Parametre() {
    }

    public Parametre(int idParametre, String cle, double valeur, String unite) {
        this.idParametre = idParametre;
        this.cle = cle;
        this.valeur = valeur;
        this.unite = unite;
    }

    public int getIdParametre() {
        return idParametre;
    }

    public void setIdParametre(int idParametre) {
        this.idParametre = idParametre;
    }

    public String getCle() {
        return cle;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }
}
