package com.hotel.service.planification;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hotel.model.Regroupement;
import com.hotel.model.Reservation;
import com.hotel.service.PlanificationService;

public class RetourVehiculeHandler {

    private final PlanificationService planificationService;
    private final ChevauchementService chevauchementService;
    private final RegroupementService regroupementService;
    private final GroupeAssignationService groupeAssignationService;
    private final RouteCalculationService routeCalculationService;

    public RetourVehiculeHandler() {
        this.planificationService = new PlanificationService();
        this.chevauchementService = new ChevauchementService();
        this.regroupementService = new RegroupementService();
        this.groupeAssignationService = new GroupeAssignationService();
        this.routeCalculationService = new RouteCalculationService();
    }

    public RetourVehiculeHandler(PlanificationService planificationService,
            ChevauchementService chevauchementService,
            RegroupementService regroupementService,
            GroupeAssignationService groupeAssignationService,
            RouteCalculationService routeCalculationService) {
        this.planificationService = planificationService;
        this.chevauchementService = chevauchementService;
        this.regroupementService = regroupementService;
        this.groupeAssignationService = groupeAssignationService;
        this.routeCalculationService = routeCalculationService;
    }

    public void onVehiculeRetour(int idVehicule, Timestamp dateRetour, Date date) throws SQLException {
        List<Reservation> reservationsNonAssignees = planificationService.getReservationsNonAssigneesByDate(date);
        if (reservationsNonAssignees.isEmpty()) {
            return;
        }

        boolean chevauchement = chevauchementService.chevaucheGroupeNormal(dateRetour, dateRetour, date);
        if (chevauchement) {
            return;
        }

        for (Reservation reservation : reservationsNonAssignees) {
            reservation.setPrioritaire(true);
            if (reservation.getDate_heure_depart_groupe() == null) {
                reservation.setDate_heure_depart_groupe(dateRetour);
            }
        }

        Regroupement regroupement = regroupementService.creerGroupeDynamique(
            dateRetour,
            reservationsNonAssignees,
            idVehicule);

        PlanificationContext context = planificationService.buildAutoPlanContext();
        List<Reservation> groupe = new ArrayList<>(reservationsNonAssignees);
        Timestamp retourInitial = routeCalculationService.calculerRetourPourGroupe(dateRetour, groupe, context);

        List<Reservation> reservationsEncoreNonAssignees = new ArrayList<>();
        List<GroupAssignment> assignations = groupeAssignationService.traiterGroupe(
                dateRetour,
                retourInitial,
                context.getAttenteMillis(),
                groupe,
                reservationsEncoreNonAssignees);

        if (!assignations.isEmpty()) {
            Timestamp departEffectif = groupeAssignationService.calculerDepartGroupeSelonAssignations(assignations, dateRetour);
            groupeAssignationService.persisterPlanificationsGroupe(
                    assignations,
                    departEffectif,
                    context,
                    true,
                    regroupement.getId());
        }
    }
}
