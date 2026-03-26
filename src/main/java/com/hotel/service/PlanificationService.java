package com.hotel.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Hotel;
import com.hotel.model.Planification;
import com.hotel.model.Reservation;
import com.hotel.service.planification.GroupAssignment;
import com.hotel.service.planification.GroupeAssignationService;
import com.hotel.service.planification.PlanificationContext;
import com.hotel.service.planification.RouteCalculationService;
import com.hotel.service.planification.RouteMetrics;
import com.hotel.service.planification.VehiculeSelectionService;

public class PlanificationService {

    private final ParametreService parametreService;
    private final ReservationService reservationService;
    private final HotelService hotelService;
    private final VehiculeSelectionService vehiculeSelectionService;
    private final RouteCalculationService routeCalculationService;
    private final GroupeAssignationService groupeAssignationService;

    public PlanificationService() {
        this.parametreService = new ParametreService();
        this.reservationService = new ReservationService();
        this.hotelService = new HotelService();
        this.vehiculeSelectionService = new VehiculeSelectionService();
        this.routeCalculationService = new RouteCalculationService();
        this.groupeAssignationService = new GroupeAssignationService();
    }

    public boolean estVoitureDisponible(int idVehicule, Timestamp dateHeureDepart, Timestamp dateHeureRetour)
            throws SQLException {
        return vehiculeSelectionService.estVoitureDisponible(idVehicule, dateHeureDepart, dateHeureRetour);
    }

    private Reservation getReservationById(int idReservation) throws SQLException {
        String sql = "SELECT r.*, h.nom as nom_hotel FROM Reservation r " +
                "LEFT JOIN Hotel h ON r.id_hotel = h.id_hotel " +
                "WHERE r.id_reservation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReservation);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setId_reservation(rs.getInt("id_reservation"));
                    reservation.setId_client(rs.getString("id_client"));
                    reservation.setNb_passager(rs.getInt("nb_passager"));
                    reservation.setDate_heure_arrivee(rs.getTimestamp("date_heure_arrivee"));
                    reservation.setId_hotel(rs.getInt("id_hotel"));
                    reservation.setNom_hotel(rs.getString("nom_hotel"));
                    return reservation;
                }
            }
        }

        return null;
    }

    

    public List<Planification> getPlanificationsByDate(java.util.Date date) throws SQLException {
        List<Planification> planifications = new ArrayList<>();

        String sql = "SELECT p.*, r.id_client, COALESCE(p.nb_passager_assigne, r.nb_passager) as nb_passager_planifie, r.id_hotel, "
                +
                "h.nom as nom_hotel, v.reference as reference_vehicule, " +
                "COALESCE(d.valeur, -1) as distance_aeroport " +
                "FROM Planification p " +
                "JOIN Reservation r ON p.id_reservation = r.id_reservation " +
                "JOIN Hotel h ON r.id_hotel = h.id_hotel " +
                "JOIN Vehicule v ON p.id_vehicule = v.id " +
                "LEFT JOIN Distance d ON d.from_hotel = 0 AND d.to_hotel = r.id_hotel " +
                "WHERE DATE(p.date_heure_depart_aeroport) = ? " +
                "ORDER BY p.date_heure_depart_aeroport";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new java.sql.Date(date.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Planification planification = new Planification();
                    planification.setIdPlanification(rs.getInt("id_planification"));
                    planification.setIdReservation(rs.getInt("id_reservation"));
                    planification.setIdVehicule(rs.getInt("id_vehicule"));
                    planification.setDateHeureDepartAeroport(rs.getTimestamp("date_heure_depart_aeroport"));
                    planification.setDateHeureRetourAeroport(rs.getTimestamp("date_heure_retour_aeroport"));
                    planification.setIdClient(rs.getString("id_client"));
                    planification.setNbPassager(rs.getInt("nb_passager_planifie"));
                    planification.setIdHotel(rs.getInt("id_hotel"));
                    planification.setNomHotel(rs.getString("nom_hotel"));
                    planification.setReferenceVehicule(rs.getString("reference_vehicule"));
                    planification.setDistanceAeroport(rs.getDouble("distance_aeroport"));
                    planifications.add(planification);
                }
            }
        }

        PlanificationContext context = buildAutoPlanContext();
        calculerOrdresDepotEtMetriques(planifications, context);

        return planifications;
    }

    private void calculerOrdresDepotEtMetriques(List<Planification> planifications, PlanificationContext context)
            throws SQLException {
        Map<String, List<Planification>> tripGroups = new LinkedHashMap<>();
        for (Planification p : planifications) {
            String key = p.getIdVehicule() + "_"
                    + p.getDateHeureDepartAeroport().getTime() + "_"
                    + p.getDateHeureRetourAeroport().getTime();
            tripGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }

        for (List<Planification> trip : tripGroups.values()) {
            trip.sort((a, b) -> {
                if (a.getDistanceAeroport() < 0 && b.getDistanceAeroport() >= 0)
                    return 1;
                if (b.getDistanceAeroport() < 0 && a.getDistanceAeroport() >= 0)
                    return -1;
                int cmp = Double.compare(a.getDistanceAeroport(), b.getDistanceAeroport());
                if (cmp != 0)
                    return cmp;
                String nomA = a.getNomHotel() != null ? a.getNomHotel() : "";
                String nomB = b.getNomHotel() != null ? b.getNomHotel() : "";
                return nomA.compareToIgnoreCase(nomB);
            });
            for (int i = 0; i < trip.size(); i++) {
                trip.get(i).setOrdreDepot(i + 1);
            }

            RouteMetrics metrics = routeCalculationService.calculerRouteDepuisPlanifications(trip, context);
            Map<Integer, double[]> hotelMetrics = routeCalculationService.calculerMetriquesParHotel(trip, context);
            for (Planification planification : trip) {
                planification.setDistanceTotaleTrajet(metrics.getTotalDistanceKm());
                double[] valeurs = hotelMetrics.get(planification.getIdHotel());
                if (valeurs != null) {
                    planification.setDistanceSegmentKm(valeurs[0]);
                    planification.setDistanceProgressiveKm(valeurs[1]);
                }
            }
        }
    }

    

    public Map<String, Object> planifierAutoParDate(java.util.Date date) throws SQLException {
        return planifierAutoParDate(date, null);
    }

    public Map<String, Object> planifierAutoParDate(java.util.Date date, Timestamp departGroupeFiltre)
            throws SQLException {
        PlanificationContext context = buildAutoPlanContext();
        List<Reservation> reservations = reservationService.getReservationByDate(date);
        List<Reservation> aTraiter = filtrerReservationsNonAssignees(reservations);
        LinkedHashMap<Long, List<Reservation>> groupes = groupeAssignationService
                .construireGroupesParFenetreAttente(aTraiter, context.getAttenteMillis());

        List<Reservation> reservationsNonAssignees = new ArrayList<>();
        for (Map.Entry<Long, List<Reservation>> entry : groupes.entrySet()) {
            Timestamp depart = new Timestamp(entry.getKey());
            List<Reservation> groupe = new ArrayList<>(entry.getValue());
            groupe.addAll(reservationsNonAssignees);
            reservationsNonAssignees.clear();

            try {
                Timestamp retourInitial = routeCalculationService.calculerRetourPourGroupe(depart, groupe, context);
                List<GroupAssignment> assignations = groupeAssignationService.traiterGroupe(
                        depart,
                        retourInitial,
                        groupe,
                        reservationsNonAssignees);

                Timestamp departGroupeAssigne = groupeAssignationService
                        .calculerDepartGroupeSelonAssignations(assignations, depart);
                groupeAssignationService.persisterPlanificationsGroupe(assignations, departGroupeAssigne, context);
            } catch (Exception e) {
                int refId = groupe.isEmpty() ? -1 : groupe.get(0).getId_reservation();
                System.err.println("Erreur de planification du groupe (reservation " + refId + "): " + e.getMessage());
                reservationsNonAssignees.addAll(groupe);
            }
        }

        List<Planification> planifications = getPlanificationsByDate(date);
        groupeAssignationService.appliquerOrdresAssignation(planifications, reservationsNonAssignees);

        if (departGroupeFiltre != null) {
            long departFiltreMillis = departGroupeFiltre.getTime();
            planifications = filtrerPlanificationsParDepart(planifications, departFiltreMillis);
            reservationsNonAssignees = filtrerReservationsNonAssigneesParDepart(reservationsNonAssignees,
                    departFiltreMillis);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("planifications", planifications);
        result.put("reservationsNonAssignees", reservationsNonAssignees);
        result.put("tempsAttenteMin", (long) (context.getAttenteMillis() / (60 * 1000)));
        return result;
    }

    private List<Planification> filtrerPlanificationsParDepart(List<Planification> planifications,
            long departFiltreMillis) {
        List<Planification> filtrees = new ArrayList<>();
        for (Planification planification : planifications) {
            Timestamp depart = planification.getDateHeureDepartAeroport();
            if (depart != null && depart.getTime() == departFiltreMillis) {
                filtrees.add(planification);
            }
        }
        return filtrees;
    }

    private List<Reservation> filtrerReservationsNonAssigneesParDepart(List<Reservation> reservations,
            long departFiltreMillis) {
        List<Reservation> filtrees = new ArrayList<>();
        for (Reservation reservation : reservations) {
            Timestamp depart = reservation.getDate_heure_depart_groupe();
            if (depart != null && depart.getTime() == departFiltreMillis) {
                filtrees.add(reservation);
            }
        }
        return filtrees;
    }

    private PlanificationContext buildAutoPlanContext() throws SQLException {
        double vitesseKmh = parametreService.getValeurByCle("vitesse_moyenne_kmh", 30.0);
        double tempsAttenteMin = parametreService.getValeurByCle("temps_attente_min", 30.0);
        long attenteMillis = (long) (tempsAttenteMin * 60 * 1000);
        Hotel aeroport = hotelService.getAeroport();
        return new PlanificationContext(vitesseKmh, attenteMillis, aeroport);
    }

    private List<Reservation> filtrerReservationsNonAssignees(List<Reservation> reservations) throws SQLException {
        List<Reservation> aTraiter = new ArrayList<>();
        Map<Integer, Integer> passagersAssignes = chargerPassagersAssignesParReservation(reservations);
        for (Reservation reservation : reservations) {
            int dejaAssigne = passagersAssignes.getOrDefault(reservation.getId_reservation(), 0);
            int passagersRestants = reservation.getNb_passager() - dejaAssigne;
            if (passagersRestants > 0) {
                aTraiter.add(groupeAssignationService.copierReservationAvecNbPassagers(reservation, passagersRestants));
            }
        }

        aTraiter.sort((a, b) -> a.getDate_heure_arrivee().compareTo(b.getDate_heure_arrivee()));
        return aTraiter;
    }

    private Map<Integer, Integer> chargerPassagersAssignesParReservation(List<Reservation> reservations)
            throws SQLException {
        Map<Integer, Integer> passagersAssignes = new HashMap<>();
        if (reservations.isEmpty()) {
            return passagersAssignes;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT p.id_reservation, SUM(COALESCE(p.nb_passager_assigne, r.nb_passager)) as nb_assigne " +
                        "FROM Planification p " +
                        "JOIN Reservation r ON r.id_reservation = p.id_reservation " +
                        "WHERE p.id_reservation IN (");
        for (int i = 0; i < reservations.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(") GROUP BY p.id_reservation");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Reservation reservation : reservations) {
                stmt.setInt(idx++, reservation.getId_reservation());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    passagersAssignes.put(rs.getInt("id_reservation"), rs.getInt("nb_assigne"));
                }
            }
        }

        return passagersAssignes;
    }
}
