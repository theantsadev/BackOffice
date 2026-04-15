package com.hotel.service.planification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hotel.model.Reservation;

class GroupeFenetreDisponibiliteTest {

    @Test
    void fenetresAncreesSurDisponibilitePlaceraC4DansDeuxiemeGroupe() {
        GroupeAssignationService service = new GroupeAssignationService();

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(resa(1, "2026-04-02 12:15:00"));
        reservations.add(resa(2, "2026-04-02 12:20:00"));
        reservations.add(resa(3, "2026-04-02 12:30:00"));
        reservations.add(resa(4, "2026-04-02 12:35:00"));

        long attenteMillis = 30L * 60L * 1000L;
        long ancre = Timestamp.valueOf("2026-04-02 12:00:00").getTime();

        LinkedHashMap<Long, List<Reservation>> groupes = service.construireGroupesParFenetreAttente(
                reservations,
                attenteMillis,
                ancre);

        assertEquals(2, groupes.size());

        List<Map.Entry<Long, List<Reservation>>> entries = new ArrayList<>(groupes.entrySet());
        assertEquals(Timestamp.valueOf("2026-04-02 12:00:00").getTime(), entries.get(0).getKey());
        assertEquals(3, entries.get(0).getValue().size());
        assertEquals(1, entries.get(0).getValue().get(0).getId_reservation());
        assertEquals(2, entries.get(0).getValue().get(1).getId_reservation());
        assertEquals(3, entries.get(0).getValue().get(2).getId_reservation());

        assertEquals(Timestamp.valueOf("2026-04-02 12:30:00").getTime(), entries.get(1).getKey());
        assertEquals(1, entries.get(1).getValue().size());
        assertEquals(4, entries.get(1).getValue().get(0).getId_reservation());
    }

    private Reservation resa(int id, String arrivee) {
        Reservation r = new Reservation();
        r.setId_reservation(id);
        r.setNb_passager(5);
        r.setDate_heure_arrivee(Timestamp.valueOf(arrivee));
        r.setId_hotel(1);
        return r;
    }
}
