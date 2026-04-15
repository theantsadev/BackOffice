package com.hotel.service.planification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hotel.model.Reservation;

class GroupeAssignationServicePriorityTest {

    @Test
    void reservationsPrioritairesSontTrieesAvantNormalesMemeAvecMoinsDePassagers() {
        GroupeAssignationService service = new GroupeAssignationService();

        Reservation normale = new Reservation();
        normale.setId_reservation(1);
        normale.setNb_passager(8);
        normale.setDate_heure_arrivee(Timestamp.valueOf("2026-04-02 09:00:00"));
        normale.setPrioritaire(false);

        Reservation prioritaire = new Reservation();
        prioritaire.setId_reservation(2);
        prioritaire.setNb_passager(2);
        prioritaire.setDate_heure_arrivee(Timestamp.valueOf("2026-04-02 09:05:00"));
        prioritaire.setPrioritaire(true);

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(normale);
        reservations.add(prioritaire);

        service.trierReservationsPourPriorite(reservations);

        assertEquals(2, reservations.get(0).getId_reservation());
        assertEquals(1, reservations.get(1).getId_reservation());
    }
}
