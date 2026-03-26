package com.hotel.service.planification;

public class VehiculeBin {
    private final int idVehicule;
    private int placesRestantes;
    private final String typeCarburant;

    public VehiculeBin(int idVehicule, int placesRestantes, String typeCarburant) {
        this.idVehicule = idVehicule;
        this.placesRestantes = placesRestantes;
        this.typeCarburant = typeCarburant;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public int getPlacesRestantes() {
        return placesRestantes;
    }

    public void setPlacesRestantes(int placesRestantes) {
        this.placesRestantes = placesRestantes;
    }

    public String getTypeCarburant() {
        return typeCarburant;
    }

    public void decrPlacesRestantes(int nbPassagers) {
        this.placesRestantes -= nbPassagers;
    }
}
