package com.hotel.service.planification;

public class AssignOrderItem {
    private final int idReservation;
    private final long departMillis;
    private final int pax;
    private final boolean assigned;
    private int ordreGroupe;
    private int ordreGlobal;

    public AssignOrderItem(int idReservation, long departMillis, int pax, boolean assigned) {
        this.idReservation = idReservation;
        this.departMillis = departMillis;
        this.pax = pax;
        this.assigned = assigned;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public long getDepartMillis() {
        return departMillis;
    }

    public int getPax() {
        return pax;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public int getOrdreGroupe() {
        return ordreGroupe;
    }

    public void setOrdreGroupe(int ordreGroupe) {
        this.ordreGroupe = ordreGroupe;
    }

    public int getOrdreGlobal() {
        return ordreGlobal;
    }

    public void setOrdreGlobal(int ordreGlobal) {
        this.ordreGlobal = ordreGlobal;
    }
}
