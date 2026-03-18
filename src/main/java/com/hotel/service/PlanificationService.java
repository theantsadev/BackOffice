package com.hotel.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Hotel;
import com.hotel.model.Planification;
import com.hotel.model.Reservation;
import com.hotel.model.Vehicule;

public class PlanificationService {

    private ParametreService parametreService = new ParametreService();
    private DistanceService distanceService = new DistanceService();
    private ReservationService reservationService = new ReservationService();
    private HotelService hotelService = new HotelService();
    private final int sprint6TieBreakerSeed = (int) (System.nanoTime() & 0x7fffffff);

    /**
     * Vérifie si un véhicule est disponible sur un créneau donné
     * Un véhicule est disponible si aucune planification existante ne chevauche le
     * créneau
     * 
     * @param idVehicule      ID du véhicule
     * @param dateHeureDepart Début du créneau
     * @param dateHeureRetour Fin du créneau
     * @return true si le véhicule est disponible
     */
    public boolean estVoitureDisponible(int idVehicule, Timestamp dateHeureDepart, Timestamp dateHeureRetour)
            throws SQLException {
        // Recherche de chevauchement :
        // Un créneau existant chevauche si : existing.depart < new.retour AND
        // existing.retour > new.depart
        String sql = "SELECT COUNT(*) FROM Planification " +
                "WHERE id_vehicule = ? " +
                "AND date_heure_depart_aeroport < ? " +
                "AND date_heure_retour_aeroport > ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVehicule);
            stmt.setTimestamp(2, dateHeureRetour);
            stmt.setTimestamp(3, dateHeureDepart);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }

        return true;
    }

    /**
     * Récupère une réservation par son ID
     * 
     * @param idReservation ID de la réservation
     * @return La réservation ou null
     */
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

    /**
     * Calcule la durée du trajet aéroport <-> hotel en minutes
     * duree_trajet = distance(aeroport, hotel) / vitesse_moyenne * 60
     * 
     * @param idReservation ID de la réservation
     * @return La durée en minutes
     */
    public double getDureeTrajetMinutes(int idReservation) throws SQLException {
        Reservation reservation = getReservationById(idReservation);
        if (reservation == null) {
            throw new SQLException("Réservation non trouvée: " + idReservation);
        }

        // Récupérer la distance aéroport -> hôtel
        double distanceKm = distanceService.getDistanceAeroportHotel(reservation.getId_hotel());
        if (distanceKm < 0) {
            throw new SQLException("Distance non trouvée pour l'hôtel: " + reservation.getId_hotel());
        }

        // Récupérer la vitesse moyenne (par défaut 30 km/h)
        double vitesseKmh = parametreService.getValeurByCle("vitesse_moyenne_kmh", 30.0);

        // Calcul: durée en heures = distance / vitesse, puis conversion en minutes
        double dureeHeures = distanceKm / vitesseKmh;
        return dureeHeures * 60;
    }

    /**
     * Calcule l'heure de départ depuis l'aéroport pour une réservation donnée
     * date_heure_depart_aeroport = date_heure_arrivee_vol + temps_attente
     * 
     * @param idReservation ID de la réservation
     * @return Le timestamp de départ de l'aéroport
     */
    public Timestamp getDateHeureDepartAeroport(int idReservation) throws SQLException {
        Reservation reservation = getReservationById(idReservation);
        if (reservation == null) {
            throw new SQLException("Réservation non trouvée: " + idReservation);
        }

        double tempsAttenteMin = parametreService.getValeurByCle("temps_attente_min", 30.0);
        long attenteMillis = (long) (tempsAttenteMin * 60 * 1000);
        return new Timestamp(reservation.getDate_heure_arrivee().getTime() + attenteMillis);
    }

    /**
     * Calcule l'heure de retour à l'aéroport (après dépôt au hotel)
     * date_heure_retour_aeroport = date_heure_depart_aeroport + 2 * duree_trajet
     * 
     * @param idReservation ID de la réservation
     * @return Le timestamp de retour à l'aéroport
     */
    public Timestamp getDateHeureRetourAeroport(int idReservation) throws SQLException {
        Reservation reservation = getReservationById(idReservation);
        if (reservation == null) {
            throw new SQLException("Réservation non trouvée: " + idReservation);
        }

        double dureeMinutes = getDureeTrajetMinutes(idReservation);
        long dureeMillis = (long) (dureeMinutes * 60 * 1000);

        // Retour = arrivée vol + durée trajet
        long retourMillis = getDateHeureDepartAeroport(idReservation).getTime() + 2 * dureeMillis;
        return new Timestamp(retourMillis);
    }

    /**
     * Sélectionne le véhicule le plus approprié pour une réservation selon Sprint
     * 6:
     * 1. nb_passager <= place du véhicule
     * 2. Choisir celui qui laisse le moins de places vides
     * 3. A capacité égale: priorité au véhicule ayant fait le moins de trajets
     * 4. Si toujours égalité: priorité au Diesel ('D')
     * 5. Si toujours égalité: tirage aléatoire
     * 6. Le véhicule doit être disponible sur le créneau
     * 
     * @param idReservation ID de la réservation
     * @return Le véhicule approprié ou null si aucun disponible
     */
    public Vehicule getVehiculeApproprieForReservation(int idReservation) throws SQLException {
        Reservation reservation = getReservationById(idReservation);
        if (reservation == null) {
            throw new SQLException("Réservation non trouvée: " + idReservation);
        }

        int nbPassagers = reservation.getNb_passager();

        // Calculer le créneau
        Timestamp depart = getDateHeureDepartAeroport(idReservation);
        Timestamp retour = getDateHeureRetourAeroport(idReservation);

        String sql = "SELECT * FROM Vehicule WHERE place >= ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nbPassagers);
            List<Vehicule> candidatsDisponibles = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id"));
                    vehicule.setReference(rs.getString("reference"));
                    vehicule.setPlace(rs.getInt("place"));
                    vehicule.setTypeCarburant(rs.getString("type_carburant"));

                    // Vérifier la disponibilité sur le créneau
                    if (estVoitureDisponible(vehicule.getId(), depart, retour)) {
                        candidatsDisponibles.add(vehicule);
                    }
                }
            }

            if (!candidatsDisponibles.isEmpty()) {
                Map<Integer, Integer> nbTrajetsByVehicule = chargerNombreTrajetsParVehicule(
                        extraireIdsVehicules(candidatsDisponibles));
                candidatsDisponibles.sort(
                        (a, b) -> comparerVehiculesSprint6(a, b, nbPassagers, nbTrajetsByVehicule));
                return candidatsDisponibles.get(0);
            }
        }

        // Aucun véhicule disponible
        return null;
    }

    /**
     * Crée une planification en base
     * 
     * @param idReservation   ID de la réservation
     * @param idVehicule      ID du véhicule
     * @param dateHeureDepart Date et heure de départ de l'aéroport
     * @param dateHeureRetour Date et heure de retour à l'aéroport
     * @return La planification créée
     */
    public Planification planifier(int idReservation, int idVehicule,
            Timestamp dateHeureDepart, Timestamp dateHeureRetour) throws SQLException {
        String sql = "INSERT INTO Planification (id_reservation, id_vehicule, " +
                "date_heure_depart_aeroport, date_heure_retour_aeroport) " +
                "VALUES (?, ?, ?, ?) RETURNING id_planification";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReservation);
            stmt.setInt(2, idVehicule);
            stmt.setTimestamp(3, dateHeureDepart);
            stmt.setTimestamp(4, dateHeureRetour);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Planification planification = new Planification();
                    planification.setIdPlanification(rs.getInt("id_planification"));
                    planification.setIdReservation(idReservation);
                    planification.setIdVehicule(idVehicule);
                    planification.setDateHeureDepartAeroport(dateHeureDepart);
                    planification.setDateHeureRetourAeroport(dateHeureRetour);
                    return planification;
                }
            }
        }

        return null;
    }

    /**
     * Récupère toutes les planifications d'une date donnée.
     * Calcule l'ordre de dépôt par vehicule :
     * 1. Distance aéroport → hôtel croissante (plus proche d'abord)
     * 2. À distance égale : ordre alphabétique sur le nom de l'hôtel
     * 
     * @param date La date pour laquelle récupérer les planifications
     * @return Liste des planifications avec infos jointes (client, hotel, vehicule,
     *         ordre_depot)
     */
    public List<Planification> getPlanificationsByDate(java.util.Date date) throws SQLException {
        List<Planification> planifications = new ArrayList<>();

        // On récupère aussi la distance aéroport (id=0) → hôtel via LEFT JOIN
        String sql = "SELECT p.*, r.id_client, r.nb_passager, r.id_hotel, " +
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
                    planification.setNbPassager(rs.getInt("nb_passager"));
                    planification.setIdHotel(rs.getInt("id_hotel"));
                    planification.setNomHotel(rs.getString("nom_hotel"));
                    planification.setReferenceVehicule(rs.getString("reference_vehicule"));
                    planification.setDistanceAeroport(rs.getDouble("distance_aeroport"));
                    planifications.add(planification);
                }
            }
        }

        // Calculer l'ordre de dépôt par groupe (même véhicule + même créneau)
        // Regrouper par (idVehicule, dateDepart, dateRetour)
        Map<String, List<Planification>> tripGroups = new LinkedHashMap<>();
        for (Planification p : planifications) {
            String key = p.getIdVehicule() + "_"
                    + p.getDateHeureDepartAeroport().getTime() + "_"
                    + p.getDateHeureRetourAeroport().getTime();
            tripGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }
        AutoPlanContext context = buildAutoPlanContext();

        // Pour chaque groupe, trier par distance ASC puis nom hôtel ASC et affecter
        // l'ordre
        for (List<Planification> trip : tripGroups.values()) {
            trip.sort((a, b) -> {
                // Distance négative = inconnue → placer en dernier
                if (a.getDistanceAeroport() < 0 && b.getDistanceAeroport() >= 0)
                    return 1;
                if (b.getDistanceAeroport() < 0 && a.getDistanceAeroport() >= 0)
                    return -1;
                int cmp = Double.compare(a.getDistanceAeroport(), b.getDistanceAeroport());
                if (cmp != 0)
                    return cmp;
                // À distance égale : ordre alphabétique sur le nom de l'hôtel
                String nomA = a.getNomHotel() != null ? a.getNomHotel() : "";
                String nomB = b.getNomHotel() != null ? b.getNomHotel() : "";
                return nomA.compareToIgnoreCase(nomB);
            });
            for (int i = 0; i < trip.size(); i++) {
                trip.get(i).setOrdreDepot(i + 1);
            }

            RouteMetrics metrics = calculerRouteDepuisPlanifications(trip, context);
            Map<Integer, double[]> hotelMetrics = calculerMetriquesParHotel(trip, context);
            for (Planification planification : trip) {
                planification.setDistanceTotaleTrajet(metrics.totalDistanceKm);
                double[] valeurs = hotelMetrics.get(planification.getIdHotel());
                if (valeurs != null) {
                    planification.setDistanceSegmentKm(valeurs[0]);
                    planification.setDistanceProgressiveKm(valeurs[1]);
                }
            }
        }

        return planifications;
    }

    /**
     * Récupère les réservations sans planification associée
     * 
     * @return Liste des réservations non assignées
     */
    public List<Reservation> getReservationsNonAssignees() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();

        String sql = "SELECT r.*, h.nom as nom_hotel FROM Reservation r " +
                "LEFT JOIN Hotel h ON r.id_hotel = h.id_hotel " +
                "WHERE r.id_reservation NOT IN (SELECT id_reservation FROM Planification) " +
                "ORDER BY r.date_heure_arrivee";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId_reservation(rs.getInt("id_reservation"));
                reservation.setId_client(rs.getString("id_client"));
                reservation.setNb_passager(rs.getInt("nb_passager"));
                reservation.setDate_heure_arrivee(rs.getTimestamp("date_heure_arrivee"));
                reservation.setId_hotel(rs.getInt("id_hotel"));
                reservation.setNom_hotel(rs.getString("nom_hotel"));
                reservations.add(reservation);
            }
        }

        return reservations;
    }

    /**
     * Vérifie si une réservation a déjà une planification associée
     * 
     * @param idReservation ID de la réservation
     * @return true si déjà assignée
     */
    public boolean isReservationDejaAssignee(int idReservation) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Planification WHERE id_reservation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReservation);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private static class VehiculeBin {
        private final int idVehicule;
        private int placesRestantes;

        VehiculeBin(int idVehicule, int placesRestantes) {
            this.idVehicule = idVehicule;
            this.placesRestantes = placesRestantes;
        }
    }

    private static class AutoPlanContext {
        private final double vitesseKmh;
        private final long attenteMillis;
        private final Hotel aeroport;
        private final Map<String, Double> distanceCache;

        AutoPlanContext(double vitesseKmh, long attenteMillis, Hotel aeroport) {
            this.vitesseKmh = vitesseKmh;
            this.attenteMillis = attenteMillis;
            this.aeroport = aeroport;
            this.distanceCache = new HashMap<>();
        }
    }

    private static class RouteMetrics {
        private final double totalDistanceKm;
        private final long totalDurationMillis;

        RouteMetrics(double totalDistanceKm, long totalDurationMillis) {
            this.totalDistanceKm = totalDistanceKm;
            this.totalDurationMillis = totalDurationMillis;
        }
    }

    private static class AssignOrderItem {
        private final int idReservation;
        private final long departMillis;
        private final int pax;
        private final boolean assigned;
        private int ordreGroupe;
        private int ordreGlobal;

        AssignOrderItem(int idReservation, long departMillis, int pax, boolean assigned) {
            this.idReservation = idReservation;
            this.departMillis = departMillis;
            this.pax = pax;
            this.assigned = assigned;
        }
    }

    /**
     * Recalcule la date_heure_retour_aeroport pour toutes les planifications d'un
     * même véhicule partant au même instant.
     */
    private void recalculerRetourVehicule(int idVehicule, Timestamp depart,
            AutoPlanContext context) throws SQLException {
        List<Integer> hotelIds = chargerHotelsDuVehicule(idVehicule, depart);
        if (hotelIds.isEmpty()) {
            return;
        }

        List<Integer> hotelsTries = trierHotelsParDistanceAeroport(hotelIds, context);
        RouteMetrics metrics = calculerRouteDepuisHotels(hotelsTries, context);
        Timestamp newRetour = new Timestamp(depart.getTime() + metrics.totalDurationMillis);
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

    /**
     * Retourne tous les véhicules libres (aucun chevauchement de créneau) avec
     * capacity >= nbPax, en excluant les IDs donnés, triés par place ASC.
     */
    private List<Vehicule> getAllVehiculesLibres(int nbPax, Timestamp depart, Timestamp retour,
            List<Integer> excludeIds) throws SQLException {
        List<Vehicule> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Vehicule WHERE place >= ?");
        if (!excludeIds.isEmpty()) {
            sql.append(" AND id NOT IN (");
            for (int i = 0; i < excludeIds.size(); i++)
                sql.append(i == 0 ? "?" : ",?");
            sql.append(")");
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setInt(idx++, nbPax);
            for (int id : excludeIds)
                stmt.setInt(idx++, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule v = new Vehicule();
                    v.setId(rs.getInt("id"));
                    v.setReference(rs.getString("reference"));
                    v.setPlace(rs.getInt("place"));
                    v.setTypeCarburant(rs.getString("type_carburant"));
                    if (estVoitureDisponible(v.getId(), depart, retour))
                        result.add(v);
                }
            }
        }

        if (!result.isEmpty()) {
            Map<Integer, Integer> nbTrajetsByVehicule = chargerNombreTrajetsParVehicule(extraireIdsVehicules(result));
            result.sort((a, b) -> comparerVehiculesSprint6(a, b, nbPax, nbTrajetsByVehicule));
        }

        return result;
    }

    /**
     * Planification automatique Sprint 5:
     * 1. Fenêtre d'attente (ex: 30 min) basée sur la première arrivée du groupe
     * 2. Départ = arrivée la plus récente dans la fenêtre
     * 3. Priorité aux réservations avec le plus de passagers
     */
    public Map<String, Object> planifierAutoParDate(java.util.Date date) throws SQLException {
        return planifierAutoParDate(date, null);
    }

    public Map<String, Object> planifierAutoParDate(java.util.Date date, Timestamp departGroupeFiltre)
            throws SQLException {
        AutoPlanContext context = buildAutoPlanContext();
        List<Reservation> reservations = reservationService.getReservationByDate(date);
        List<Reservation> aTraiter = filtrerReservationsNonAssignees(reservations);
        System.out.println(aTraiter.size() + " réservations à traiter pour la date " + date);
        LinkedHashMap<Long, List<Reservation>> groupes = construireGroupesParFenetreAttente(aTraiter,
                context.attenteMillis);
        Map<Integer, Timestamp> departGroupeByReservation = new HashMap<>();

        List<Reservation> reservationsNonAssignees = new ArrayList<>();
        for (Map.Entry<Long, List<Reservation>> entry : groupes.entrySet()) {
            Timestamp depart = new Timestamp(entry.getKey());
            List<Reservation> groupe = new ArrayList<>(entry.getValue());
            groupe.addAll(reservationsNonAssignees);
            reservationsNonAssignees.clear();

            try {
                Timestamp retourInitial = calculerRetourPourGroupe(depart, groupe, context);
                traiterGroupe(depart, retourInitial, groupe, context, reservationsNonAssignees);
            } catch (Exception e) {
                int refId = groupe.isEmpty() ? -1 : groupe.get(0).getId_reservation();
                System.err.println("Erreur de planification du groupe (réservation " + refId + "): " + e.getMessage());
                reservationsNonAssignees.addAll(groupe);
            }

            Timestamp departGroupeAssigne = calculerDepartGroupeSelonAssignations(groupe, reservationsNonAssignees,
                    depart);
            for (Reservation reservation : groupe) {
                departGroupeByReservation.put(reservation.getId_reservation(), departGroupeAssigne);
            }
        }

        List<Planification> planifications = getPlanificationsByDate(date);
        appliquerOrdresAssignation(planifications, reservationsNonAssignees, departGroupeByReservation);

        if (departGroupeFiltre != null) {
            long departFiltreMillis = departGroupeFiltre.getTime();
            planifications = filtrerPlanificationsParDepart(planifications, departFiltreMillis);
            reservationsNonAssignees = filtrerReservationsNonAssigneesParDepart(reservationsNonAssignees,
                    departFiltreMillis);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("planifications", planifications);
        result.put("reservationsNonAssignees", reservationsNonAssignees);
        result.put("tempsAttenteMin", (long) (context.attenteMillis / (60 * 1000)));
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

    private void appliquerOrdresAssignation(List<Planification> planifications,
            List<Reservation> reservationsNonAssignees,
            Map<Integer, Timestamp> departGroupeByReservation) {
        List<AssignOrderItem> items = new ArrayList<>();

        for (Planification planification : planifications) {
            Timestamp departGroupe = departGroupeByReservation.get(planification.getIdReservation());
            if (departGroupe != null && planification.getDateHeureDepartAeroport() != null) {
                long deltaMillis = departGroupe.getTime() - planification.getDateHeureDepartAeroport().getTime();
                planification.setDateHeureDepartAeroport(departGroupe);
                if (planification.getDateHeureRetourAeroport() != null) {
                    planification.setDateHeureRetourAeroport(
                            new Timestamp(planification.getDateHeureRetourAeroport().getTime() + deltaMillis));
                }
            }

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
            Timestamp departGroupe = departGroupeByReservation.get(reservation.getId_reservation());
            if (departGroupe == null) {
                departGroupe = reservation.getDate_heure_arrivee();
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
        items.sort((a, b) -> Long.compare(a.departMillis, b.departMillis));
        for (AssignOrderItem item : items) {
            byDepart.computeIfAbsent(item.departMillis, k -> new ArrayList<>()).add(item);
        }

        for (List<AssignOrderItem> group : byDepart.values()) {
            group.sort((a, b) -> {
                int paxCmp = Integer.compare(b.pax, a.pax);
                if (paxCmp != 0) {
                    return paxCmp;
                }
                return Integer.compare(a.idReservation, b.idReservation);
            });
            for (int i = 0; i < group.size(); i++) {
                group.get(i).ordreGroupe = i + 1;
            }
        }

        items.sort((a, b) -> {
            int departCmp = Long.compare(a.departMillis, b.departMillis);
            if (departCmp != 0) {
                return departCmp;
            }
            int groupeCmp = Integer.compare(a.ordreGroupe, b.ordreGroupe);
            if (groupeCmp != 0) {
                return groupeCmp;
            }
            return Integer.compare(a.idReservation, b.idReservation);
        });
        for (int i = 0; i < items.size(); i++) {
            items.get(i).ordreGlobal = i + 1;
        }

        Map<Integer, AssignOrderItem> assignedMap = new HashMap<>();
        Map<Integer, AssignOrderItem> nonAssignedMap = new HashMap<>();
        for (AssignOrderItem item : items) {
            if (item.assigned) {
                assignedMap.put(item.idReservation, item);
            } else {
                nonAssignedMap.put(item.idReservation, item);
            }
        }

        for (Planification planification : planifications) {
            AssignOrderItem item = assignedMap.get(planification.getIdReservation());
            if (item != null) {
                planification.setOrdreAssignGroupe(item.ordreGroupe);
                planification.setOrdreAssignGlobal(item.ordreGlobal);
            }
        }

        for (Reservation reservation : reservationsNonAssignees) {
            AssignOrderItem item = nonAssignedMap.get(reservation.getId_reservation());
            if (item != null) {
                reservation.setOrdre_assign_groupe(item.ordreGroupe);
                reservation.setOrdre_assign_global(item.ordreGlobal);
            }
        }
    }

    private AutoPlanContext buildAutoPlanContext() throws SQLException {
        double vitesseKmh = parametreService.getValeurByCle("vitesse_moyenne_kmh", 30.0);
        double tempsAttenteMin = parametreService.getValeurByCle("temps_attente_min", 30.0);
        long attenteMillis = (long) (tempsAttenteMin * 60 * 1000);
        Hotel aeroport = hotelService.getAeroport();
        return new AutoPlanContext(vitesseKmh, attenteMillis, aeroport);
    }

    private List<Reservation> filtrerReservationsNonAssignees(List<Reservation> reservations) throws SQLException {
        List<Reservation> aTraiter = new ArrayList<>();
        Set<Integer> dejaAssignees = chargerReservationIdsDejaAssignees(reservations);
        for (Reservation reservation : reservations) {
            if (!dejaAssignees.contains(reservation.getId_reservation())) {
                aTraiter.add(reservation);
            }
        }

        aTraiter.sort((a, b) -> a.getDate_heure_arrivee().compareTo(b.getDate_heure_arrivee()));
        return aTraiter;
    }

    private Set<Integer> chargerReservationIdsDejaAssignees(List<Reservation> reservations) throws SQLException {
        Set<Integer> assignees = new HashSet<>();
        if (reservations.isEmpty()) {
            return assignees;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT id_reservation FROM Planification WHERE id_reservation IN (");
        for (int i = 0; i < reservations.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Reservation reservation : reservations) {
                stmt.setInt(idx++, reservation.getId_reservation());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assignees.add(rs.getInt("id_reservation"));
                }
            }
        }

        return assignees;
    }

    private LinkedHashMap<Long, List<Reservation>> construireGroupesParFenetreAttente(
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

            // Règle Sprint 5: départ = arrivée la plus récente de la fenêtre.
            long depart = groupe.get(groupe.size() - 1).getDate_heure_arrivee().getTime();
            groupes.put(depart, groupe);
        }

        return groupes;
    }

    private Timestamp calculerRetourPourGroupe(Timestamp depart, List<Reservation> groupe,
            AutoPlanContext context) throws SQLException {
        List<Integer> hotelIds = new ArrayList<>();
        for (Reservation reservation : groupe) {
            if (!hotelIds.contains(reservation.getId_hotel())) {
                hotelIds.add(reservation.getId_hotel());
            }
        }

        List<Integer> hotelsTries = trierHotelsParDistanceAeroport(hotelIds, context);
        RouteMetrics metrics = calculerRouteDepuisHotels(hotelsTries, context);
        return new Timestamp(depart.getTime() + metrics.totalDurationMillis);
    }

    private void traiterGroupe(Timestamp depart,
            Timestamp retourInitial,
            List<Reservation> groupe,
            AutoPlanContext context,
            List<Reservation> reservationsNonAssignees) throws SQLException {
        groupe.sort((a, b) -> Integer.compare(b.getNb_passager(), a.getNb_passager()));

        List<Integer> vehiculesUtilises = new ArrayList<>();
        List<VehiculeBin> bins = chargerBinsExistants(depart, vehiculesUtilises);

        for (int i = 0; i < groupe.size(); i++) {
            Reservation reservation = groupe.get(i);
            int bestBinIdx = trouverMeilleurBin(bins, reservation.getNb_passager());

            try {
                if (bestBinIdx >= 0) {
                    assignerSurBinExistant(reservation, depart, retourInitial, bins.get(bestBinIdx), context);
                } else {
                    boolean assignee = ouvrirNouveauVehiculeEtAssigner(
                            reservation,
                            depart,
                            retourInitial,
                            context,
                            vehiculesUtilises,
                            bins,
                            groupe.subList(i + 1, groupe.size()));
                    if (!assignee) {
                        reservationsNonAssignees.add(reservation);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur planification auto pour réservation "
                        + reservation.getId_reservation() + ": " + e.getMessage());
                reservationsNonAssignees.add(reservation);
            }
        }
    }

    private List<VehiculeBin> chargerBinsExistants(Timestamp depart, List<Integer> vehiculesUtilises) {
        List<VehiculeBin> bins = new ArrayList<>();
        String sql = "SELECT p.id_vehicule, v.place, COALESCE(SUM(r.nb_passager),0) as places_prises " +
                "FROM Planification p " +
                "JOIN Vehicule v ON v.id = p.id_vehicule " +
                "JOIN Reservation r ON r.id_reservation = p.id_reservation " +
                "WHERE p.date_heure_depart_aeroport = ? " +
                "GROUP BY p.id_vehicule, v.place";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, depart);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idVehicule = rs.getInt("id_vehicule");
                    int placesLibres = rs.getInt("place") - rs.getInt("places_prises");
                    bins.add(new VehiculeBin(idVehicule, placesLibres));
                    vehiculesUtilises.add(idVehicule);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement bins existants: " + e.getMessage());
        }
        return bins;
    }

    private int trouverMeilleurBin(List<VehiculeBin> bins, int nbPassagers) {
        int bestIdx = -1;
        int bestReste = Integer.MAX_VALUE;

        for (int i = 0; i < bins.size(); i++) {
            int reste = bins.get(i).placesRestantes;
            if (reste >= nbPassagers && reste < bestReste) {
                bestReste = reste;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private void assignerSurBinExistant(Reservation reservation,
            Timestamp depart,
            Timestamp retourInitial,
            VehiculeBin bin,
            AutoPlanContext context) throws SQLException {
        bin.placesRestantes -= reservation.getNb_passager();
        planifier(reservation.getId_reservation(), bin.idVehicule, depart, retourInitial);
        recalculerRetourVehicule(bin.idVehicule, depart, context);
    }

    private boolean ouvrirNouveauVehiculeEtAssigner(Reservation reservation,
            Timestamp depart,
            Timestamp retourInitial,
            AutoPlanContext context,
            List<Integer> vehiculesUtilises,
            List<VehiculeBin> bins,
            List<Reservation> restantes) throws SQLException {
        Vehicule vehicule = choisirVehiculeAvecLookAhead(
                reservation.getNb_passager(), depart, retourInitial, vehiculesUtilises, restantes);

        if (vehicule == null) {
            return false;
        }

        vehiculesUtilises.add(vehicule.getId());
        bins.add(new VehiculeBin(vehicule.getId(), vehicule.getPlace() - reservation.getNb_passager()));
        planifier(reservation.getId_reservation(), vehicule.getId(), depart, retourInitial);
        recalculerRetourVehicule(vehicule.getId(), depart, context);
        return true;
    }

    private Vehicule choisirVehiculeAvecLookAhead(int nbPax,
            Timestamp depart,
            Timestamp retour,
            List<Integer> vehiculesUtilises,
            List<Reservation> restantes) throws SQLException {
        List<Vehicule> candidats = getAllVehiculesLibres(nbPax, depart, retour, vehiculesUtilises);
        Vehicule meilleurVehicule = null;
        int meilleurGaspillage = Integer.MAX_VALUE;
        Map<Integer, Integer> nbTrajetsByVehicule = chargerNombreTrajetsParVehicule(extraireIdsVehicules(candidats));

        for (Vehicule candidat : candidats) {
            int placesRestantes = candidat.getPlace() - nbPax;
            int gaspillage = evaluerGaspillage(placesRestantes, restantes);
            if (gaspillage < meilleurGaspillage
                    || (gaspillage == meilleurGaspillage && meilleurVehicule != null
                            && comparerVehiculesSprint6(candidat, meilleurVehicule, nbPax, nbTrajetsByVehicule) < 0)) {
                meilleurGaspillage = gaspillage;
                meilleurVehicule = candidat;
            } else if (gaspillage == meilleurGaspillage && meilleurVehicule == null) {
                meilleurVehicule = candidat;
            }
        }

        return meilleurVehicule;
    }

    private Timestamp calculerDepartGroupeSelonAssignations(List<Reservation> groupe,
            List<Reservation> reservationsNonAssignees,
            Timestamp fallback) {
        Set<Integer> nonAssigneesIds = new HashSet<>();
        for (Reservation reservation : reservationsNonAssignees) {
            nonAssigneesIds.add(reservation.getId_reservation());
        }

        Timestamp maxArriveeAssignee = null;
        for (Reservation reservation : groupe) {
            if (nonAssigneesIds.contains(reservation.getId_reservation())) {
                continue;
            }

            Timestamp arrivee = reservation.getDate_heure_arrivee();
            if (arrivee != null && (maxArriveeAssignee == null || arrivee.after(maxArriveeAssignee))) {
                maxArriveeAssignee = arrivee;
            }
        }

        return maxArriveeAssignee != null ? maxArriveeAssignee : fallback;
    }

    private List<Integer> extraireIdsVehicules(List<Vehicule> vehicules) {
        List<Integer> ids = new ArrayList<>();
        for (Vehicule vehicule : vehicules) {
            ids.add(vehicule.getId());
        }
        return ids;
    }

    private Map<Integer, Integer> chargerNombreTrajetsParVehicule(List<Integer> vehiculeIds) throws SQLException {
        Map<Integer, Integer> nbTrajetsByVehicule = new HashMap<>();
        if (vehiculeIds.isEmpty()) {
            return nbTrajetsByVehicule;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT id_vehicule, COUNT(DISTINCT date_heure_depart_aeroport) AS nb_trajets FROM Planification WHERE id_vehicule IN (");
        for (int i = 0; i < vehiculeIds.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(") GROUP BY id_vehicule");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Integer vehiculeId : vehiculeIds) {
                stmt.setInt(idx++, vehiculeId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    nbTrajetsByVehicule.put(rs.getInt("id_vehicule"), rs.getInt("nb_trajets"));
                }
            }
        }

        return nbTrajetsByVehicule;
    }

    private int comparerVehiculesSprint6(Vehicule a,
            Vehicule b,
            int nbPassagers,
            Map<Integer, Integer> nbTrajetsByVehicule) {
        int videA = a.getPlace() - nbPassagers;
        int videB = b.getPlace() - nbPassagers;
        int cmpCapacite = Integer.compare(videA, videB);
        if (cmpCapacite != 0) {
            return cmpCapacite;
        }

        int trajetsA = nbTrajetsByVehicule.getOrDefault(a.getId(), 0);
        int trajetsB = nbTrajetsByVehicule.getOrDefault(b.getId(), 0);
        int cmpTrajets = Integer.compare(trajetsA, trajetsB);
        if (cmpTrajets != 0) {
            return cmpTrajets;
        }

        boolean dieselA = "D".equalsIgnoreCase(a.getTypeCarburant());
        boolean dieselB = "D".equalsIgnoreCase(b.getTypeCarburant());
        if (dieselA != dieselB) {
            return dieselA ? -1 : 1;
        }

        int tieA = melangerPourTieBreak(a.getId());
        int tieB = melangerPourTieBreak(b.getId());
        int cmpRandomLike = Integer.compare(tieA, tieB);
        if (cmpRandomLike != 0) {
            return cmpRandomLike;
        }

        return Integer.compare(a.getId(), b.getId());
    }

    private int melangerPourTieBreak(int vehiculeId) {
        int x = vehiculeId ^ sprint6TieBreakerSeed;
        x ^= (x << 13);
        x ^= (x >>> 17);
        x ^= (x << 5);
        return x & 0x7fffffff;
    }

    private int evaluerGaspillage(int placesRestantes, List<Reservation> restantes) {
        List<Integer> paxRestants = new ArrayList<>();
        for (Reservation reservation : restantes) {
            paxRestants.add(reservation.getNb_passager());
        }

        Collections.sort(paxRestants);
        int gaspillage = placesRestantes;
        for (int pax : paxRestants) {
            if (pax <= gaspillage) {
                gaspillage -= pax;
            }
        }
        return gaspillage;
    }

    private RouteMetrics calculerRouteDepuisPlanifications(List<Planification> trip, AutoPlanContext context)
            throws SQLException {
        List<Integer> hotelIds = new ArrayList<>();
        for (Planification planification : trip) {
            if (!hotelIds.contains(planification.getIdHotel())) {
                hotelIds.add(planification.getIdHotel());
            }
        }

        List<Integer> hotelsTries = trierHotelsParDistanceAeroport(hotelIds, context);
        return calculerRouteDepuisHotels(hotelsTries, context);
    }

    private Map<Integer, double[]> calculerMetriquesParHotel(List<Planification> trip, AutoPlanContext context)
            throws SQLException {
        List<Integer> hotelIds = new ArrayList<>();
        for (Planification planification : trip) {
            if (!hotelIds.contains(planification.getIdHotel())) {
                hotelIds.add(planification.getIdHotel());
            }
        }

        List<Integer> hotelsTries = trierHotelsParDistanceAeroport(hotelIds, context);
        Map<Integer, double[]> metricsByHotel = new HashMap<>();

        int precedent = context.aeroport.getId_hotel();
        double progressif = 0;
        for (int hotelId : hotelsTries) {
            double segmentKm = safeDistanceKm(precedent, hotelId, context);
            progressif += segmentKm;
            metricsByHotel.put(hotelId, new double[] { segmentKm, progressif });
            precedent = hotelId;
        }

        return metricsByHotel;
    }

    private List<Integer> trierHotelsParDistanceAeroport(List<Integer> hotelIds, AutoPlanContext context) {
        List<Integer> sorted = new ArrayList<>(hotelIds);
        sorted.sort((a, b) -> {
            try {
                return Double.compare(
                        safeDistanceKm(context.aeroport.getId_hotel(), a, context),
                        safeDistanceKm(context.aeroport.getId_hotel(), b, context));
            } catch (SQLException e) {
                return 0;
            }
        });
        return sorted;
    }

    private RouteMetrics calculerRouteDepuisHotels(List<Integer> hotelIds, AutoPlanContext context)
            throws SQLException {
        double distanceTotaleKm = 0;
        long dureeTotaleMillis = 0;
        int hotelActuel = context.aeroport.getId_hotel();

        for (int hotelId : hotelIds) {
            double segmentKm = safeDistanceKm(hotelActuel, hotelId, context);
            distanceTotaleKm += segmentKm;
            dureeTotaleMillis += convertDistanceToMillis(segmentKm, context.vitesseKmh);
            hotelActuel = hotelId;
        }

        double retourKm = safeDistanceKm(hotelActuel, context.aeroport.getId_hotel(), context);
        distanceTotaleKm += retourKm;
        dureeTotaleMillis += convertDistanceToMillis(retourKm, context.vitesseKmh);

        return new RouteMetrics(distanceTotaleKm, dureeTotaleMillis);
    }

    private double safeDistanceKm(int fromHotelId, int toHotelId, AutoPlanContext context) throws SQLException {
        String key = fromHotelId < toHotelId
                ? fromHotelId + "_" + toHotelId
                : toHotelId + "_" + fromHotelId;

        Double cached = context.distanceCache.get(key);
        if (cached != null) {
            return cached;
        }

        double distanceKm = distanceService.getDistanceValeur(fromHotelId, toHotelId);
        double safeDistance = distanceKm < 0 ? 0 : distanceKm;
        context.distanceCache.put(key, safeDistance);
        return safeDistance;
    }

    private long convertDistanceToMillis(double distanceKm, double vitesseKmh) {
        double dureeHeures = distanceKm / vitesseKmh;
        return (long) (dureeHeures * 3600 * 1000);
    }
}
