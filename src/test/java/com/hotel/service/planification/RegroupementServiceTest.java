package com.hotel.service.planification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hotel.model.Regroupement;
import com.hotel.model.Reservation;

class RegroupementServiceTest {

    @Test
    void creationGroupeDynamiqueCorrecte() throws SQLException {
        final List<Timestamp> capturedDebut = new ArrayList<>();
        final List<Timestamp> capturedFin = new ArrayList<>();
        final List<String> capturedType = new ArrayList<>();

        RegroupementService service = new RegroupementService() {
            @Override
            protected Regroupement insererRegroupement(Timestamp dateDebut,
                    Timestamp dateFin,
                    String type,
                    Timestamp dateTrigger,
                    Integer idVehiculeTrigger) throws SQLException {
                capturedDebut.add(dateDebut);
                capturedFin.add(dateFin);
                capturedType.add(type);
                Regroupement regroupement = new Regroupement();
                regroupement.setId(77);
                regroupement.setDateDebut(dateDebut);
                regroupement.setDateFin(dateFin);
                regroupement.setType(type);
                regroupement.setDateTrigger(dateTrigger);
                regroupement.setIdVehiculeTrigger(idVehiculeTrigger);
                return regroupement;
            }
        };

        List<Reservation> nonAssignees = new ArrayList<>();
        Reservation r = new Reservation();
        r.setDate_heure_arrivee(Timestamp.valueOf("2026-04-02 10:05:00"));
        nonAssignees.add(r);

        Timestamp trigger = Timestamp.valueOf("2026-04-02 10:00:00");
        Regroupement regroupement = service.creerGroupeDynamique(trigger, nonAssignees);

        assertEquals(1, capturedDebut.size());
        assertEquals(trigger, capturedDebut.get(0));
        assertEquals("DYNAMIQUE", capturedType.get(0));
        assertEquals(77, regroupement.getId());
    }

    @Test
    void getGroupesNormauxRetourneUniquementNormal() throws SQLException {
        RegroupementService service = new RegroupementService() {
            @Override
            protected List<Regroupement> chargerRegroupementsParDate(Date date) throws SQLException {
                List<Regroupement> all = new ArrayList<>();
                Regroupement normal = new Regroupement();
                normal.setType("NORMAL");
                all.add(normal);

                Regroupement dynamique = new Regroupement();
                dynamique.setType("DYNAMIQUE");
                all.add(dynamique);
                return all;
            }
        };

        List<Regroupement> normaux = service
                .getGroupesNormaux(Date.from(Timestamp.valueOf("2026-04-02 00:00:00").toInstant()));

        assertEquals(1, normaux.size());
        assertEquals("NORMAL", normaux.get(0).getType());
    }
}
