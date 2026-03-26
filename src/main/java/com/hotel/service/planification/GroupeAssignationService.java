package com.hotel.service.planification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Planification;
import com.hotel.model.Reservation;
import com.hotel.model.Vehicule;

public class GroupeAssignationService {

    private final VehiculeSelectionService vehiculeSelectionService;
    private final RouteCalculationService routeCalculationService;

    public GroupeAssignationService() {
        this.vehiculeSelectionService = new VehiculeSelectionService();
        this.routeCalculationService = new RouteCalculationService();
    }

    public GroupeAssignationService(VehiculeSelectionService vehiculeSelectionService,
            RouteCalculationService routeCalculationService) {
        this.vehiculeSelectionService = vehiculeSelectionService;
        this.routeCalculationService = routeCalculationService;
    }

    public LinkedHashMap<Long, List<Reservation>> construireGroupesParFenetreAttente(
            List<Reservation> reservationsTriees,
            long attenteMillis) {
        LinkedHashMap<Long, List<Reservation>> groupes = new LinkedHashMap<>();
        int index = 0;

        while (index < reservationsTriees.size()) {
            Reservation premiere = reservationsTriees.get(index);
            long debutFenetre = premiere.getDate_heure_arrivee().getTime();
            long finFenetre = debutFenetre + attenteMillis;

            List<Reservation> groupe = new ArrayList<>();
            groupe.add(premiere);
            index++;

            while (index < reservationsTriees.size()) {
                Reservation candidate = reservationsTriees.get(index);
                if (candidate.getDate_heure_arrivee().getTime() <= finFenetre) {
                    groupe.add(candidate);
                    index++;
                } else {
                    break;
                }
            }

            long depart = groupe.get(groupe.size() - 1).getDate_heure_arrivee().getTime();
            groupes.put(depart, groupe);
        }

        return groupes;
    }

    public List<GroupAssignment> traiterGroupe(Timestamp depart,
            Timestamp retourInitial,
            List<Reservation> groupe,
            List<Reservation> reservationsNonAssignees) throws SQLException {
        List<GroupAssignment> assignations = new ArrayList<>();
        groupe.sort((a, b) -> Integer.compare(b.getNb_passager(), a.getNb_passager()));

        List<Integer> vehiculesUtilises = new ArrayList<>();
        List<VehiculeBin> bins = chargerBinsExistants(depart, vehiculesUtilises);

        for (int i = 0; i < groupe.size(); i++) {
            Reservation reservation = groupe.get(i);
            int passagersRestants = reservation.getNb_passager();

            try {
                while (passagersRestants > 0) {
                    int bestBinIdx = vehiculeSelectionService.trouverMeilleurBin(bins, passagersRestants);
                    Vehicule meilleurNouveauVehicule = vehiculeSelectionService.choisirVehiculeAvecLookAhead(
                            passagersRestants,
                            depart,
                            retourInitial,
                            vehiculesUtilises,
                            groupe.subList(i + 1, groupe.size()));
                    Vehicule nouveauVehicule = meilleurNouveauVehicule;

                    boolean peutAssignerBin = bestBinIdx >= 0;
                    boolean peutOuvrirNouveau = meilleurNouveauVehicule != null;

                    if (!peutAssignerBin && !peutOuvrirNouveau) {
                        break;
                    }

                    if (peutAssignerBin && !peutOuvrirNouveau) {
                        VehiculeBin bin = bins.get(bestBinIdx);
                        int passagersAffectes = Math.min(passagersRestants, bin.getPlacesRestantes());
                        GroupAssignment assignation = assignerSurBinExistant(reservation, bin, passagersAffectes);
                        assignations.add(assignation);
                        passagersRestants -= passagersAffectes;
                        continue;
                    }

                    if (!peutAssignerBin) {
                        if (nouveauVehicule == null) {
                            break;
                        }
                        int passagersAffectes = Math.min(passagersRestants, nouveauVehicule.getPlace());
                        GroupAssignment assignation = assignerSurNouveauVehicule(reservation, vehiculesUtilises,
                                bins, nouveauVehicule, passagersAffectes);
                        assignations.add(assignation);
                        passagersRestants -= passagersAffectes;
                        continue;
                    }

                    VehiculeBin binCandidat = bins.get(bestBinIdx);
                    boolean prendreNouveau = vehiculeSelectionService.doitPrendreNouveauVehicule(
                            binCandidat,
                            nouveauVehicule,
                            passagersRestants);

                    if (prendreNouveau) {
                        int passagersAffectes = Math.min(passagersRestants, nouveauVehicule.getPlace());
                        GroupAssignment assignation = assignerSurNouveauVehicule(reservation, vehiculesUtilises,
                                bins, nouveauVehicule, passagersAffectes);
                        assignations.add(assignation);
                        passagersRestants -= passagersAffectes;
                    } else {
                        int passagersAffectes = Math.min(passagersRestants, binCandidat.getPlacesRestantes());
                        GroupAssignment assignation = assignerSurBinExistant(reservation, binCandidat,
                                passagersAffectes);
                        assignations.add(assignation);
                        passagersRestants -= passagersAffectes;
                    }
                }

                if (passagersRestants > 0) {
                    Reservation restante = copierReservationAvecNbPassagers(reservation, passagersRestants);
                    restante.setDate_heure_depart_groupe(depart);
                    reservationsNonAssignees.add(restante);
                }

            } catch (Exception e) {
                System.err.println("Erreur planification auto pour reservation "
                        + reservation.getId_reservation() + ": " + e.getMessage());
                Reservation restante = copierReservationAvecNbPassagers(reservation, passagersRestants);
                restante.setDate_heure_depart_groupe(depart);
                reservationsNonAssignees.add(restante);
            }
        }

        return assignations;
    }

    public List<VehiculeBin> chargerBinsExistants(Timestamp depart, List<Integer> vehiculesUtilises) {
        List<VehiculeBin> bins = new ArrayList<>();
        String sql = "SELECT p.id_vehicule, v.place, v.type_carburant, COALESCE(SUM(COALESCE(p.nb_passager_assigne, r.nb_passager)),0) as places_prises "
                +
                "FROM Planification p " +
                "JOIN Vehicule v ON v.id = p.id_vehicule " +
                "JOIN Reservation r ON r.id_reservation = p.id_reservation " +
                "WHERE p.date_heure_depart_aeroport = ? " +
                "GROUP BY p.id_vehicule, v.place, v.type_carburant";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, depart);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idVehicule = rs.getInt("id_vehicule");
                    int placesLibres = rs.getInt("place") - rs.getInt("places_prises");
                    String typeCarburant = rs.getString("type_carburant");
                    bins.add(new VehiculeBin(idVehicule, placesLibres, typeCarburant));
                    vehiculesUtilises.add(idVehicule);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement bins existants: " + e.getMessage());
        }
        return bins;
    }

    private GroupAssignment assignerSurBinExistant(Reservation reservation,
            VehiculeBin bin,
            int nbPassagersAffectes) {
        bin.decrPlacesRestantes(nbPassagersAffectes);
        return new GroupAssignment(reservation, bin.getIdVehicule(), nbPassagersAffectes);
    }

    private GroupAssignment assignerSurNouveauVehicule(Reservation reservation,
            List<Integer> vehiculesUtilises,
            List<VehiculeBin> bins,
            Vehicule vehicule,
            int nbPassagersAffectes) {
        vehiculesUtilises.add(vehicule.getId());
        bins.add(new VehiculeBin(
                vehicule.getId(),
                vehicule.getPlace() - nbPassagersAffectes,
                vehicule.getTypeCarburant()));
        return new GroupAssignment(reservation, vehicule.getId(), nbPassagersAffectes);
    }

    public Timestamp calculerDepartGroupeSelonAssignations(List<GroupAssignment> assignations,
            Timestamp fallback) {
        Timestamp maxArriveeAssignee = null;
        for (GroupAssignment assignation : assignations) {
            Timestamp arrivee = assignation.getReservation().getDate_heure_arrivee();
            if (arrivee != null && (maxArriveeAssignee == null || arrivee.after(maxArriveeAssignee))) {
                maxArriveeAssignee = arrivee;
            }
        }

        return maxArriveeAssignee != null ? maxArriveeAssignee : fallback;
    }

    public void persisterPlanificationsGroupe(List<GroupAssignment> assignations,
            Timestamp departGroupe,
            PlanificationContext context) throws SQLException {
        if (assignations.isEmpty()) {
            return;
        }

        Map<Integer, Timestamp> retourByVehicule = calculerRetoursParVehicule(assignations, departGroupe, context);
        String sql = "INSERT INTO Planification (id_reservation, id_vehicule, date_heure_depart_aeroport, date_heure_retour_aeroport, nb_passager_assigne) "
                +
                "VALUES (?, ?, ?, ?, ?)";

        Set<Integer> vehiculesARecalculer = new HashSet<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                for (GroupAssignment assignation : assignations) {
                    Timestamp retour = retourByVehicule.get(assignation.getIdVehicule());
                    if (retour == null) {
                        throw new SQLException("Retour introuvable pour le vehicule " + assignation.getIdVehicule());
                    }

                    stmt.setInt(1, assignation.getReservation().getId_reservation());
                    stmt.setInt(2, assignation.getIdVehicule());
                    stmt.setTimestamp(3, departGroupe);
                    stmt.setTimestamp(4, retour);
                    stmt.setInt(5, assignation.getNbPassagersAssignes());
                    stmt.addBatch();
                    vehiculesARecalculer.add(assignation.getIdVehicule());
                }

                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }

        for (Integer idVehicule : vehiculesARecalculer) {
            recalculerRetourVehicule(idVehicule, departGroupe, context);
        }
    }

    private Map<Integer, Timestamp> calculerRetoursParVehicule(List<GroupAssignment> assignations,
            Timestamp depart,
            PlanificationContext context) throws SQLException {
        Map<Integer, List<Integer>> hotelsByVehicule = new HashMap<>();
        for (GroupAssignment assignation : assignations) {
            List<Integer> hotels = hotelsByVehicule.computeIfAbsent(assignation.getIdVehicule(), k -> new ArrayList<>());
            int idHotel = assignation.getReservation().getId_hotel();
            if (!hotels.contains(idHotel)) {
                hotels.add(idHotel);
            }
        }

        Map<Integer, Timestamp> retourByVehicule = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : hotelsByVehicule.entrySet()) {
            List<Integer> hotelsTries = routeCalculationService.trierHotelsParDistanceAeroport(entry.getValue(),
                    context);
            RouteMetrics metrics = routeCalculationService.calculerRouteDepuisHotels(hotelsTries, context);
            retourByVehicule.put(entry.getKey(), new Timestamp(depart.getTime() + metrics.getTotalDurationMillis()));
        }

        return retourByVehicule;
    }

    public void recalculerRetourVehicule(int idVehicule, Timestamp depart,
            PlanificationContext context) throws SQLException {
        List<Integer> hotelIds = chargerHotelsDuVehicule(idVehicule, depart);
        if (hotelIds.isEmpty()) {
            return;
        }

        List<Integer> hotelsTries = routeCalculationService.trierHotelsParDistanceAeroport(hotelIds, context);
        RouteMetrics metrics = routeCalculationService.calculerRouteDepuisHotels(hotelsTries, context);
        Timestamp newRetour = new Timestamp(depart.getTime() + metrics.getTotalDurationMillis());
        updateRetourVehicule(idVehicule, depart, newRetour);
    }

    private List<Integer> chargerHotelsDuVehicule(int idVehicule, Timestamp depart) throws SQLException {
        String sqlHotels = "SELECT DISTINCT r.id_hotel FROM Planification p " +
                "JOIN Reservation r ON r.id_reservation = p.id_reservation " +
                "WHERE p.id_vehicule = ? AND p.date_heure_depart_aeroport = ? " +
                "ORDER BY r.id_hotel";

        List<Integer> hotelIds = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlHotels)) {
            stmt.setInt(1, idVehicule);
            stmt.setTimestamp(2, depart);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hotelIds.add(rs.getInt("id_hotel"));
                }
            }
        }
        return hotelIds;
    }

    private void updateRetourVehicule(int idVehicule, Timestamp depart, Timestamp retour) throws SQLException {
        String sqlUpdate = "UPDATE Planification SET date_heure_retour_aeroport = ? " +
                "WHERE id_vehicule = ? AND date_heure_depart_aeroport = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setTimestamp(1, retour);
            stmt.setInt(2, idVehicule);
            stmt.setTimestamp(3, depart);
            stmt.executeUpdate();
        }
    }

    public void appliquerOrdresAssignation(List<Planification> planifications,
            List<Reservation> reservationsNonAssignees) {
        List<AssignOrderItem> items = new ArrayList<>();

        for (Planification planification : planifications) {
            long departMillis = planification.getDateHeureDepartAeroport() != null
                    ? planification.getDateHeureDepartAeroport().getTime()
                    : 0;
            items.add(new AssignOrderItem(
                    planification.getIdReservation(),
                    departMillis,
                    planification.getNbPassager(),
                    true));
        }

        for (Reservation reservation : reservationsNonAssignees) {
            Timestamp departGroupe = reservation.getDate_heure_depart_groupe();
            if (departGroupe == null) {
                departGroupe = reservation.getDate_heure_arrivee();
                reservation.setDate_heure_depart_groupe(departGroupe);
            }
            reservation.setDate_heure_depart_groupe(departGroupe);
            long departMillis = departGroupe != null ? departGroupe.getTime() : 0;
            items.add(new AssignOrderItem(
                    reservation.getId_reservation(),
                    departMillis,
                    reservation.getNb_passager(),
                    false));
        }

        Map<Long, List<AssignOrderItem>> byDepart = new LinkedHashMap<>();
        items.sort((a, b) -> Long.compare(a.getDepartMillis(), b.getDepartMillis()));
        for (AssignOrderItem item : items) {
            byDepart.computeIfAbsent(item.getDepartMillis(), k -> new ArrayList<>()).add(item);
        }

        for (List<AssignOrderItem> group : byDepart.values()) {
            group.sort((a, b) -> {
                int paxCmp = Integer.compare(b.getPax(), a.getPax());
                if (paxCmp != 0) {
                    return paxCmp;
                }
                return Integer.compare(a.getIdReservation(), b.getIdReservation());
            });
            for (int i = 0; i < group.size(); i++) {
                group.get(i).setOrdreGroupe(i + 1);
            }
        }

        items.sort((a, b) -> {
            int departCmp = Long.compare(a.getDepartMillis(), b.getDepartMillis());
            if (departCmp != 0) {
                return departCmp;
            }
            int groupeCmp = Integer.compare(a.getOrdreGroupe(), b.getOrdreGroupe());
            if (groupeCmp != 0) {
                return groupeCmp;
            }
            return Integer.compare(a.getIdReservation(), b.getIdReservation());
        });
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setOrdreGlobal(i + 1);
        }

        Map<Integer, List<AssignOrderItem>> assignedMap = new HashMap<>();
        Map<Integer, List<AssignOrderItem>> nonAssignedMap = new HashMap<>();
        for (AssignOrderItem item : items) {
            if (item.isAssigned()) {
                assignedMap.computeIfAbsent(item.getIdReservation(), k -> new ArrayList<>()).add(item);
            } else {
                nonAssignedMap.computeIfAbsent(item.getIdReservation(), k -> new ArrayList<>()).add(item);
            }
        }

        for (Planification planification : planifications) {
            AssignOrderItem item = extrairePremierItem(assignedMap, planification.getIdReservation());
            if (item != null) {
                planification.setOrdreAssignGroupe(item.getOrdreGroupe());
                planification.setOrdreAssignGlobal(item.getOrdreGlobal());
            }
        }

        for (Reservation reservation : reservationsNonAssignees) {
            AssignOrderItem item = extrairePremierItem(nonAssignedMap, reservation.getId_reservation());
            if (item != null) {
                reservation.setOrdre_assign_groupe(item.getOrdreGroupe());
                reservation.setOrdre_assign_global(item.getOrdreGlobal());
            }
        }
    }

    private AssignOrderItem extrairePremierItem(Map<Integer, List<AssignOrderItem>> map, int idReservation) {
        List<AssignOrderItem> items = map.get(idReservation);
        if (items == null || items.isEmpty()) {
            return null;
        }

        AssignOrderItem premier = items.remove(0);
        if (items.isEmpty()) {
            map.remove(idReservation);
        }
        return premier;
    }

    public Reservation copierReservationAvecNbPassagers(Reservation source, int nbPassagers) {
        Reservation copie = new Reservation();
        copie.setId_reservation(source.getId_reservation());
        copie.setId_client(source.getId_client());
        copie.setNb_passager(nbPassagers);
        copie.setDate_heure_arrivee(source.getDate_heure_arrivee());
        copie.setDate_heure_depart_groupe(source.getDate_heure_depart_groupe());
        copie.setId_hotel(source.getId_hotel());
        copie.setNom_hotel(source.getNom_hotel());
        return copie;
    }
}
