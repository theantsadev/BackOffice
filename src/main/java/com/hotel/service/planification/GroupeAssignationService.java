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

    /**
     * Construit des groupes de réservations en fonction d'une fenêtre d'attente
     *
     * @param reservationsTriees Liste des réservations triées
     * @param attenteMillis      Durée de la fenêtre d'attente en millisecondes
     * @return Map contenant les groupes de réservations
     */
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

    /**
     * =====================================================================
     * SPRINT 7 : Traitement d'un groupe avec l'algorithme Best-Fit
     * =====================================================================
     *
     * ALGORITHME :
     *
     * 1. TRIER les réservations par nb_passagers DÉCROISSANT
     *
     * 2. TANT QUE il reste des réservations non assignées :
     * a) Prendre la PREMIÈRE résa (la plus grosse)
     * b) Trouver le véhicule Best-Fit (place >= nb_pax, gaspillage minimal)
     * - Si aucun véhicule assez grand → prendre le plus grand (scission)
     * - Si véhicule en retour est meilleur → ajuster date départ
     * c) REMPLIR CE VÉHICULE avant d'en ouvrir un autre :
     * - Chercher dans les résas restantes celle qui minimize |nb_pax -
     * places_restantes|
     * - Répéter jusqu'à véhicule plein ou aucune résa ne rentre
     * d) Si aucun véhicule dispo → reporter au prochain groupe
     *
     * 3. Les résas non assignées sont reportées au prochain groupe d'attente
     */
    public List<GroupAssignment> traiterGroupe(Timestamp depart,
            Timestamp retourInitial,
            long attenteMaxMillis,
            List<Reservation> groupe,
            List<Reservation> reservationsNonAssignees) throws SQLException {

        List<GroupAssignment> assignations = new ArrayList<>();

        // ============================================================
        // ÉTAPE 1 : Tri du groupe par nb_passagers DÉCROISSANT
        // ============================================================
        groupe.sort((a, b) -> Integer.compare(b.getNb_passager(), a.getNb_passager()));

        // Liste des réservations restantes à traiter (avec leurs passagers restants)
        List<ReservationRestante> resasRestantes = new ArrayList<>();
        for (Reservation r : groupe) {
            resasRestantes.add(new ReservationRestante(r, r.getNb_passager()));
        }

        // Liste des véhicules déjà utilisés
        List<Integer> vehiculesUtilises = new ArrayList<>();

        // Charger les bins existants (véhicules déjà ouverts sur ce créneau)
        List<VehiculeBin> bins = chargerBinsExistants(depart, vehiculesUtilises);

        // Date de départ effective (peut être ajustée si on attend un véhicule)
        Timestamp departEffectif = depart;

        // ============================================================
        // ÉTAPE 2 : Traiter les réservations
        // ============================================================
        boolean reportRestantes = false;
        while (!resasRestantes.isEmpty()) {
            trierResasRestantesParPaxDesc(resasRestantes);

            // 2a : On traite d'abord la réservation courante jusqu'au bout
            // (même si elle est scindée sur plusieurs véhicules).
            ReservationRestante resaCourante = resasRestantes.get(0);

            while (resaCourante.passagersRestants > 0) {
                // 2a-1 : Essayer d'abord un véhicule déjà ouvert (bin existant)
                int binIdx = trouverMeilleurBinPourReservation(bins, resaCourante.passagersRestants);
                if (binIdx >= 0) {
                    VehiculeBin bin = bins.get(binIdx);
                    int passagersAAffectes = Math.min(resaCourante.passagersRestants, bin.getPlacesRestantes());

                    assignations.add(new GroupAssignment(
                            resaCourante.reservation,
                            bin.getIdVehicule(),
                            passagersAAffectes,
                            departEffectif));

                    bin.decrPlacesRestantes(passagersAAffectes);
                    resaCourante.passagersRestants -= passagersAAffectes;

                    if (bin.getPlacesRestantes() <= 0) {
                        bins.remove(binIdx);
                    }

                    continue;
                }

                // 2a-2 : Sinon, ouvrir un nouveau véhicule pour continuer la même résa
                VehiculeRetour vehiculeTrouve = vehiculeSelectionService.trouverVehiculeBestFitAvecAttente(
                        resaCourante.passagersRestants,
                        departEffectif,
                        retourInitial,
                        vehiculesUtilises);

                if (vehiculeTrouve == null) {
                    reportRestantes = true;
                    break;
                }

                Vehicule vehicule = vehiculeTrouve.getVehicule();
                Timestamp departVehicule = vehiculeTrouve.getDateRetour();

                if (departVehicule.after(departEffectif)) {
                    long attenteVehiculeMillis = departVehicule.getTime() - depart.getTime();
                    if (attenteVehiculeMillis > attenteMaxMillis) {
                        reportRestantes = true;
                        break;
                    }
                    departEffectif = departVehicule;
                }

                vehiculesUtilises.add(vehicule.getId());
                int placesRestantes = vehicule.getPlace();

                int paxCible = Math.min(resaCourante.passagersRestants, placesRestantes);
                assignations.add(new GroupAssignment(
                        resaCourante.reservation,
                        vehicule.getId(),
                        paxCible,
                        departEffectif));

                placesRestantes -= paxCible;
                resaCourante.passagersRestants -= paxCible;

                // Une fois la résa courante servie sur ce véhicule, remplir le reste
                // avec la résa la plus proche en nombre de passagers.
                while (placesRestantes > 0 && !resasRestantes.isEmpty()) {
                    int meilleurIdx = trouverResaBestFitPourVehicule(resasRestantes, placesRestantes);
                    if (meilleurIdx < 0) {
                        break;
                    }

                    ReservationRestante resaChoisie = resasRestantes.get(meilleurIdx);
                    if (resaChoisie == resaCourante && resaCourante.passagersRestants > 0) {
                        break;
                    }

                    int passagersAAffectes = Math.min(resaChoisie.passagersRestants, placesRestantes);
                    assignations.add(new GroupAssignment(
                            resaChoisie.reservation,
                            vehicule.getId(),
                            passagersAAffectes,
                            departEffectif));

                    placesRestantes -= passagersAAffectes;
                    resaChoisie.passagersRestants -= passagersAAffectes;

                    if (resaChoisie.passagersRestants <= 0) {
                        resasRestantes.remove(meilleurIdx);
                    }
                }

                if (placesRestantes > 0) {
                    bins.add(new VehiculeBin(vehicule.getId(), placesRestantes, vehicule.getTypeCarburant()));
                }
            }

            if (reportRestantes) {
                reporterReservationsRestantes(resasRestantes, departEffectif, reservationsNonAssignees);
                break;
            }

            if (resaCourante.passagersRestants <= 0) {
                resasRestantes.remove(resaCourante);
            }
        }

        return assignations;
    }

    private int trouverMeilleurBinPourReservation(List<VehiculeBin> bins, int passagersRestants) {
        int meilleurBinIdx = -1;
        int meilleurEcart = Integer.MAX_VALUE;

        for (int i = 0; i < bins.size(); i++) {
            VehiculeBin bin = bins.get(i);
            int placesRestantes = bin.getPlacesRestantes();
            if (placesRestantes <= 0) {
                continue;
            }

            int ecart = Math.abs(placesRestantes - passagersRestants);
            if (ecart < meilleurEcart) {
                meilleurEcart = ecart;
                meilleurBinIdx = i;
                if (ecart == 0) {
                    break;
                }
            }
        }

        return meilleurBinIdx;
    }

    private void reporterReservationsRestantes(List<ReservationRestante> resasRestantes,
            Timestamp departEffectif,
            List<Reservation> reservationsNonAssignees) {
        for (ReservationRestante rr : resasRestantes) {
            if (rr.passagersRestants > 0) {
                Reservation restante = copierReservationAvecNbPassagers(rr.reservation, rr.passagersRestants);
                restante.setDate_heure_depart_groupe(departEffectif);
                reservationsNonAssignees.add(restante);
            }
        }
    }

    private void trierResasRestantesParPaxDesc(List<ReservationRestante> resasRestantes) {
        resasRestantes.sort((a, b) -> {
            int cmpPax = Integer.compare(b.passagersRestants, a.passagersRestants);
            if (cmpPax != 0) {
                return cmpPax;
            }

            Timestamp arriveeA = a.reservation.getDate_heure_arrivee();
            Timestamp arriveeB = b.reservation.getDate_heure_arrivee();
            if (arriveeA != null && arriveeB != null) {
                int cmpArrivee = arriveeA.compareTo(arriveeB);
                if (cmpArrivee != 0) {
                    return cmpArrivee;
                }
            }

            return Integer.compare(
                    a.reservation.getId_reservation(),
                    b.reservation.getId_reservation());
        });
    }

    /**
     * Trouve la réservation qui minimise |nb_pax - places_restantes| (Best-Fit pour
     * remplissage).
     *
     * @param resasRestantes  liste des réservations avec leurs passagers restants
     * @param placesRestantes nombre de places restantes dans le véhicule
     * @return index de la meilleure résa, ou -1 si aucune ne peut rentrer
     */
    private int trouverResaBestFitPourVehicule(List<ReservationRestante> resasRestantes, int placesRestantes) {
        int meilleurIdx = -1;
        int meilleurEcart = Integer.MAX_VALUE;
        int meilleurNbPax = Integer.MAX_VALUE;

        for (int i = 0; i < resasRestantes.size(); i++) {
            int nbPax = resasRestantes.get(i).passagersRestants;

            if (nbPax <= 0)
                continue;

            // Best-fit strict: minimiser |placesRestantes - nbPax|.
            // En dépassement, cela favorise la réservation qui dépasse le moins.
            int ecart = Math.abs(placesRestantes - nbPax);

            if (ecart < meilleurEcart || (ecart == meilleurEcart && nbPax < meilleurNbPax)) {
                meilleurEcart = ecart;
                meilleurIdx = i;
                meilleurNbPax = nbPax;

                // Si écart = 0, c'est parfait (véhicule plein ou exactement la bonne taille)
                if (ecart == 0)
                    break;
            }
        }

        return meilleurIdx;
    }

    /**
     * Classe interne pour suivre les passagers restants d'une réservation.
     */
    private static class ReservationRestante {
        final Reservation reservation;
        int passagersRestants;

        ReservationRestante(Reservation reservation, int passagersRestants) {
            this.reservation = reservation;
            this.passagersRestants = passagersRestants;
        }
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

        // Sprint 7 : Calculer les retours par véhicule en tenant compte des dates de
        // départ individuelles
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
                    // Sprint 7 : Utiliser la date de départ de l'assignation si disponible
                    Timestamp departEffectif = assignation.getDateDepart() != null
                            ? assignation.getDateDepart()
                            : departGroupe;

                    Timestamp retour = retourByVehicule.get(assignation.getIdVehicule());
                    if (retour == null) {
                        throw new SQLException("Retour introuvable pour le vehicule " + assignation.getIdVehicule());
                    }

                    stmt.setInt(1, assignation.getReservation().getId_reservation());
                    stmt.setInt(2, assignation.getIdVehicule());
                    stmt.setTimestamp(3, departEffectif); // ← Utilise la date effective
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

    /**
     * Calcule la date/heure de retour pour chaque véhicule utilisé.
     * Sprint 7 : Utilise la date de départ de chaque assignation si disponible.
     */
    private Map<Integer, Timestamp> calculerRetoursParVehicule(List<GroupAssignment> assignations,
            Timestamp departDefaut,
            PlanificationContext context) throws SQLException {

        // Regrouper les hôtels et trouver la date de départ par véhicule
        Map<Integer, List<Integer>> hotelsByVehicule = new HashMap<>();
        Map<Integer, Timestamp> departByVehicule = new HashMap<>();

        for (GroupAssignment assignation : assignations) {
            int idVehicule = assignation.getIdVehicule();

            // Ajouter l'hôtel à la liste du véhicule
            List<Integer> hotels = hotelsByVehicule.computeIfAbsent(idVehicule, k -> new ArrayList<>());
            int idHotel = assignation.getReservation().getId_hotel();
            if (!hotels.contains(idHotel)) {
                hotels.add(idHotel);
            }

            // Sprint 7 : Trouver la date de départ pour ce véhicule (prendre la plus
            // tardive si plusieurs)
            Timestamp departAssignation = assignation.getDateDepart() != null
                    ? assignation.getDateDepart()
                    : departDefaut;

            Timestamp departActuel = departByVehicule.get(idVehicule);
            if (departActuel == null || departAssignation.after(departActuel)) {
                departByVehicule.put(idVehicule, departAssignation);
            }
        }

        // Calculer le retour pour chaque véhicule
        Map<Integer, Timestamp> retourByVehicule = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : hotelsByVehicule.entrySet()) {
            int idVehicule = entry.getKey();
            Timestamp depart = departByVehicule.get(idVehicule);

            List<Integer> hotelsTries = routeCalculationService.trierHotelsParDistanceAeroport(entry.getValue(),
                    context);
            RouteMetrics metrics = routeCalculationService.calculerRouteDepuisHotels(hotelsTries, context);
            retourByVehicule.put(idVehicule, new Timestamp(depart.getTime() + metrics.getTotalDurationMillis()));
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
