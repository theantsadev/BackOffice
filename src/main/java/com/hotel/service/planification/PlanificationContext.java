package com.hotel.service.planification;

import java.util.HashMap;
import java.util.Map;

import com.hotel.model.Hotel;

public class PlanificationContext {
    private final double vitesseKmh;
    private final long attenteMillis;
    private final Hotel aeroport;
    private final Map<String, Double> distanceCache;

    public PlanificationContext(double vitesseKmh, long attenteMillis, Hotel aeroport) {
        this.vitesseKmh = vitesseKmh;
        this.attenteMillis = attenteMillis;
        this.aeroport = aeroport;
        this.distanceCache = new HashMap<>();
    }

    public double getVitesseKmh() {
        return vitesseKmh;
    }

    public long getAttenteMillis() {
        return attenteMillis;
    }

    public Hotel getAeroport() {
        return aeroport;
    }

    public Map<String, Double> getDistanceCache() {
        return distanceCache;
    }
}
