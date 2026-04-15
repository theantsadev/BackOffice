package com.hotel.model;

import java.sql.Timestamp;

public class Regroupement {
    private int id;
    private Timestamp dateDebut;
    private Timestamp dateFin;
    private String type;
    private Timestamp dateTrigger;
    private Integer idVehiculeTrigger;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Timestamp dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Timestamp getDateFin() {
        return dateFin;
    }

    public void setDateFin(Timestamp dateFin) {
        this.dateFin = dateFin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getDateTrigger() {
        return dateTrigger;
    }

    public void setDateTrigger(Timestamp dateTrigger) {
        this.dateTrigger = dateTrigger;
    }

    public Integer getIdVehiculeTrigger() {
        return idVehiculeTrigger;
    }

    public void setIdVehiculeTrigger(Integer idVehiculeTrigger) {
        this.idVehiculeTrigger = idVehiculeTrigger;
    }
}
