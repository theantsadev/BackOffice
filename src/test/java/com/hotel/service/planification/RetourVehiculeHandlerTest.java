package com.hotel.service.planification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hotel.model.Hotel;
import com.hotel.model.Regroupement;
import com.hotel.model.Reservation;
import com.hotel.service.PlanificationService;

class RetourVehiculeHandlerTest {

    @Test
    void pasDeNonAssigneesHandlerNeFaitRien() throws SQLException {
        FakePlanificationService planification = new FakePlanificationService(new ArrayList<>());
        FakeChevauchementService chevauchement = new FakeChevauchementService(false);
        FakeRegroupementService regroupement = new FakeRegroupementService();
        FakeGroupeAssignationService groupeAssignation = new FakeGroupeAssignationService();

        RetourVehiculeHandler handler = new RetourVehiculeHandler(
                planification,
                chevauchement,
                regroupement,
                groupeAssignation,
                new RouteCalculationService());

        handler.onVehiculeRetour(1, Timestamp.valueOf("2026-04-02 10:00:00"), new Date());

        assertEquals(0, regroupement.dynamicCreationCount);
        assertEquals(0, groupeAssignation.persistCount);
    }

    @Test
    void chevauchementDetectePasDeGroupeDynamique() throws SQLException {
        List<Reservation> nonAssignees = new ArrayList<>();
        Reservation r = new Reservation();
        r.setId_reservation(10);
        r.setNb_passager(2);
        r.setDate_heure_arrivee(Timestamp.valueOf("2026-04-02 09:00:00"));
        r.setId_hotel(1);
        nonAssignees.add(r);

        FakePlanificationService planification = new FakePlanificationService(nonAssignees);
        FakeChevauchementService chevauchement = new FakeChevauchementService(true);
        FakeRegroupementService regroupement = new FakeRegroupementService();
        FakeGroupeAssignationService groupeAssignation = new FakeGroupeAssignationService();

        RetourVehiculeHandler handler = new RetourVehiculeHandler(
                planification,
                chevauchement,
                regroupement,
                groupeAssignation,
                new RouteCalculationService());

        handler.onVehiculeRetour(1, Timestamp.valueOf("2026-04-02 10:00:00"), new Date());

        assertEquals(0, regroupement.dynamicCreationCount);
        assertEquals(0, groupeAssignation.persistCount);
    }

    @Test
    void sansChevauchementGroupeDynamiqueCreeEtPlanifie() throws SQLException {
        List<Reservation> nonAssignees = new ArrayList<>();
        Reservation r = new Reservation();
        r.setId_reservation(11);
        r.setNb_passager(3);
        r.setDate_heure_arrivee(Timestamp.valueOf("2026-04-02 10:00:00"));
        r.setId_hotel(1);
        nonAssignees.add(r);

        FakePlanificationService planification = new FakePlanificationService(nonAssignees);
        FakeChevauchementService chevauchement = new FakeChevauchementService(false);
        FakeRegroupementService regroupement = new FakeRegroupementService();
        FakeGroupeAssignationService groupeAssignation = new FakeGroupeAssignationService();

        RetourVehiculeHandler handler = new RetourVehiculeHandler(
                planification,
                chevauchement,
                regroupement,
                groupeAssignation,
                new RouteCalculationService());

        handler.onVehiculeRetour(1, Timestamp.valueOf("2026-04-02 11:00:00"), new Date());

        assertEquals(1, regroupement.dynamicCreationCount);
        assertEquals(1, groupeAssignation.persistCount);
    }

    static class FakePlanificationService extends PlanificationService {
        private final List<Reservation> reservations;

        FakePlanificationService(List<Reservation> reservations) {
            this.reservations = reservations;
        }

        @Override
        public List<Reservation> getReservationsNonAssigneesByDate(Date date) {
            return reservations;
        }

        @Override
        public PlanificationContext buildAutoPlanContext() {
            Hotel aeroport = new Hotel();
            aeroport.setId_hotel(0);
            aeroport.setNom("Aeroport");
            return new PlanificationContext(30.0, 30L * 60L * 1000L, aeroport);
        }
    }

    static class FakeChevauchementService extends ChevauchementService {
        private final boolean chevauche;

        FakeChevauchementService(boolean chevauche) {
            this.chevauche = chevauche;
        }

        @Override
        public boolean chevaucheGroupeNormal(Timestamp debut, Timestamp fin, Date date) {
            return chevauche;
        }
    }

    static class FakeRegroupementService extends RegroupementService {
        int dynamicCreationCount;

        @Override
        public Regroupement creerGroupeDynamique(
                Timestamp trigger,
                List<Reservation> nonAssignees,
                Integer idVehiculeTrigger) {
            dynamicCreationCount++;
            Regroupement regroupement = new Regroupement();
            regroupement.setId(999);
            regroupement.setType("DYNAMIQUE");
            regroupement.setDateDebut(trigger);
            regroupement.setDateFin(trigger);
            regroupement.setIdVehiculeTrigger(idVehiculeTrigger);
            return regroupement;
        }
    }

    static class FakeGroupeAssignationService extends GroupeAssignationService {
        int persistCount;

        @Override
        public List<GroupAssignment> traiterGroupe(Timestamp depart,
                Timestamp retourInitial,
                long attenteMaxMillis,
                List<Reservation> groupe,
                List<Reservation> reservationsNonAssignees) {
            List<GroupAssignment> out = new ArrayList<>();
            if (!groupe.isEmpty()) {
                out.add(new GroupAssignment(groupe.get(0), 1, groupe.get(0).getNb_passager(), depart));
            }
            return out;
        }

        @Override
        public void persisterPlanificationsGroupe(List<GroupAssignment> assignations,
                Timestamp departGroupe,
                PlanificationContext context,
                boolean dynamique,
                Integer idRegroupement) {
            persistCount++;
        }
    }
}
