package com.hotel.model;

public class Vehicule {
    private int id;
    private String reference;
    private int place;
    private String typeCarburant; // D = Diesel, E = Essence

    public Vehicule() {
    }

    public Vehicule(int id, String reference, int place, String typeCarburant) {
        this.id = id;
        this.reference = reference;
        this.place = place;
        setTypeCarburant(typeCarburant);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public String getTypeCarburant() {
        return typeCarburant;
    }

    public void setTypeCarburant(String typeCarburant) {
        if (typeCarburant != null && (typeCarburant.equals("D") || typeCarburant.equals("E"))) {
            this.typeCarburant = typeCarburant;
        } else {
            throw new IllegalArgumentException("Type carburant doit être 'D' (Diesel) ou 'E' (Essence)");
        }
    }

    public String getTypeCarburantLibelle() {
        if ("D".equals(typeCarburant)) {
            return "Diesel";
        } else if ("E".equals(typeCarburant)) {
            return "Essence";
        }
        return "Inconnu";
    }
}
