package com.hotel.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Distance;
import com.hotel.model.Hotel;
import com.hotel.model.Planification;
import com.hotel.model.Reservation;
import com.hotel.model.Vehicule;

public class PlanificationService {

    private ParametreService parametreService = new ParametreService();
    private DistanceService distanceService = new DistanceService();
    private VehiculeService vehiculeService = new VehiculeService();
    private ReservationService reservationService = new ReservationService();
    private HotelService hotelService = new HotelService();

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
     * Sélectionne le véhicule le plus approprié pour une réservation selon les
     * règles de gestion:
     * 1. nb_passager <= place du véhicule
     * 2. Choisir celui qui laisse le moins de places vides
     * 3. Si égalité → priorité au Diesel ('D')
     * 4. Si égalité de capacité ET de type → random
     * 5. Le véhicule doit être disponible sur le créneau
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

        // Récupérer tous les véhicules avec assez de places, triés selon les règles
        // ORDER BY: 1) places - nb_passager ASC (moins de places vides)
        // 2) type_carburant = 'D' DESC (Diesel en priorité)
        // 3) RANDOM() pour départager
        String sql = "SELECT * FROM Vehicule " +
                "WHERE place >= ? " +
                "ORDER BY (place - ?) ASC, " +
                "CASE WHEN type_carburant = 'D' THEN 0 ELSE 1 END ASC, " +
                "RANDOM()";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nbPassagers);
            stmt.setInt(2, nbPassagers);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id"));
                    vehicule.setReference(rs.getString("reference"));
                    vehicule.setPlace(rs.getInt("place"));
                    vehicule.setTypeCarburant(rs.getString("type_carburant"));

                    // Vérifier la disponibilité sur le créneau
                    if (estVoitureDisponible(vehicule.getId(), depart, retour)) {
                        return vehicule;
                    }
                }
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

    /**
     * Recalcule la date_heure_retour_aeroport pour TOUTES les planifications
     * d'un véhicule partant au même instant (covoiturage).
     * Route : aéroport → hôtels triés par distance croissante → aéroport.
     * Met à jour en DB toutes les lignes concernées.
     */
    private void recalculerRetourVehicule(int idVehicule, Timestamp depart,
            Hotel aeroport, double vitesseKmh) throws SQLException {
        // 1. Récupérer tous les id_hotel distincts des réservations de ce
        // véhicule+départ
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
                while (rs.next())
                    hotelIds.add(rs.getInt("id_hotel"));
            }
        }
        if (hotelIds.isEmpty())
            return;

        // 2. Trier par distance depuis l'aéroport (croissante)
        hotelIds.sort((a, b) -> {
            try {
                return Double.compare(
                        distanceService.getDistanceAeroportHotel(a),
                        distanceService.getDistanceAeroportHotel(b));
            } catch (SQLException e) {
                return 0;
            }
        });

        // 3. Calculer la durée totale du trajet : aéroport → h1 → h2 → ... → aéroport
        long dureeTotal = 0;
        int currentHotel = aeroport.getId_hotel();
        for (int hId : hotelIds) {
            double distKm = distanceService.getDistanceValeur(currentHotel, hId);
            if (distKm < 0)
                distKm = 0;
            dureeTotal += (long) ((distKm / vitesseKmh) * 3600 * 1000);
            currentHotel = hId;
        }
        // Retour à l'aéroport
        double distRetour = distanceService.getDistanceValeur(currentHotel, aeroport.getId_hotel());
        if (distRetour < 0)
            distRetour = 0;
        dureeTotal += (long) ((distRetour / vitesseKmh) * 3600 * 1000);

        Timestamp newRetour = new Timestamp(depart.getTime() + dureeTotal);

        // 4. UPDATE toutes les planifications de ce véhicule+départ
        String sqlUpdate = "UPDATE Planification SET date_heure_retour_aeroport = ? " +
                "WHERE id_vehicule = ? AND date_heure_depart_aeroport = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setTimestamp(1, newRetour);
            stmt.setInt(2, idVehicule);
            stmt.setTimestamp(3, depart);
            stmt.executeUpdate();
        }
    }

    /**
     * Trouve le véhicule le plus approprié pour un créneau donné.
     * Priorité 1 : véhicule déjà en covoiturage sur ce créneau EXACT avec encore
     * assez de places libres (best-fit sur les places restantes).
     * Priorité 2 : véhicule totalement libre sur ce créneau (best-fit classique).
     *
     * @param nbPassagers Nombre de places requises
     * @param depart      Timestamp de départ aéroport
     * @param retour      Timestamp de retour aéroport
     * @return Le véhicule choisi ou null si aucun disponible
     */
    private Vehicule trouverVehiculeDisponible(int nbPassagers, Timestamp depart, Timestamp retour,
            List<Integer> excludeIds) throws SQLException {

        // --- Priorité 1 : covoiturage en cours sur ce même départ ---
        // Chercher les véhicules qui ont déjà une planification avec le même depart
        String sqlCovoit = "SELECT v.*, COALESCE(SUM(r.nb_passager), 0) as places_prises " +
                "FROM Vehicule v " +
                "JOIN Planification p ON p.id_vehicule = v.id " +
                "JOIN Reservation r ON r.id_reservation = p.id_reservation " +
                "WHERE p.date_heure_depart_aeroport = ? " +
                "GROUP BY v.id, v.reference, v.place, v.type_carburant " +
                "HAVING (v.place - COALESCE(SUM(r.nb_passager), 0)) >= ? " +
                "ORDER BY (v.place - COALESCE(SUM(r.nb_passager), 0)) ASC, " +
                "CASE WHEN v.type_carburant = 'D' THEN 0 ELSE 1 END ASC, RANDOM()";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlCovoit)) {
            stmt.setTimestamp(1, depart);
            stmt.setInt(2, nbPassagers);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id"));
                    vehicule.setReference(rs.getString("reference"));
                    vehicule.setPlace(rs.getInt("place"));
                    vehicule.setTypeCarburant(rs.getString("type_carburant"));
                    return vehicule; // premier = best-fit sur places restantes
                }
            }
        }

        // --- Priorité 2 : véhicule libre (aucun chevauchement) ---
        StringBuilder sql = new StringBuilder("SELECT * FROM Vehicule WHERE place >= ?");
        if (!excludeIds.isEmpty()) {
            sql.append(" AND id NOT IN (");
            for (int i = 0; i < excludeIds.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
            }
            sql.append(")");
        }
        sql.append(" ORDER BY (place - ?) ASC, ")
                .append("CASE WHEN type_carburant = 'D' THEN 0 ELSE 1 END ASC, ")
                .append("RANDOM()");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setInt(idx++, nbPassagers);
            for (int id : excludeIds) {
                stmt.setInt(idx++, id);
            }
            stmt.setInt(idx, nbPassagers);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id"));
                    vehicule.setReference(rs.getString("reference"));
                    vehicule.setPlace(rs.getInt("place"));
                    vehicule.setTypeCarburant(rs.getString("type_carburant"));
                    if (estVoitureDisponible(vehicule.getId(), depart, retour)) {
                        return vehicule;
                    }
                }
            }
        }
        return null;
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
            for (int i = 0; i < excludeIds.size(); i++) sql.append(i == 0 ? "?" : ",?");
            sql.append(")");
        }
        sql.append(" ORDER BY place ASC, CASE WHEN type_carburant = 'D' THEN 0 ELSE 1 END ASC");
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setInt(idx++, nbPax);
            for (int id : excludeIds) stmt.setInt(idx++, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule v = new Vehicule();
                    v.setId(rs.getInt("id"));
                    v.setReference(rs.getString("reference"));
                    v.setPlace(rs.getInt("place"));
                    v.setTypeCarburant(rs.getString("type_carburant"));
                    if (estVoitureDisponible(v.getId(), depart, retour)) result.add(v);
                }
            }
        }
        return result;
    }

    /**
     * Planification automatique pour toutes les réservations d'une date donnée.
     * Les réservations arrivant à la même heure et à destination du même hôtel
     * sont regroupées dans un même véhicule (covoiturage) selon l'algorithme
     * First-Fit Decreasing (FFD): les plus grands groupes placés en premier,
     * puis les autres remplissent les places libres des véhicules déjà ouverts.
     *
     * Exemple: voiture_A(10 places), voiture_B(6 places), voiture_C(4 places),
     * client_1(7 pax) + client_2(3 pax) arrivant au même moment → voiture_A.
     *
     * @param date La date des réservations
     * @return Map avec "planifications" et "reservationsNonAssignees"
     */
    public Map<String, Object> planifierAutoParDate(java.util.Date date) throws SQLException {
        List<Reservation> reservations = reservationService.getReservationByDate(date);
        double vitesseKmh = parametreService.getValeurByCle("vitesse_moyenne_kmh", 30.0);
        double tempsAttenteMin = parametreService.getValeurByCle("temps_attente_min", 30.0);
        long attenteMillis = (long) (tempsAttenteMin * 60 * 1000);
        Hotel hotel = hotelService.getAeroport();

        // Filtrer les réservations déjà assignées
        List<Reservation> aTraiter = new ArrayList<>();
        for (Reservation r : reservations) {
            if (!isReservationDejaAssignee(r.getId_reservation())) {
                aTraiter.add(r);
            }
        }

        // Trier par heure d'arrivée pour construire les fenêtres temporelles
        aTraiter.sort((a, b) -> {
            int cmp = a.getDate_heure_arrivee().compareTo(b.getDate_heure_arrivee());
            return cmp;
        });

        // Regrouper par fenêtre d'attente:
        // le groupe démarre à la première arrivée t0, et inclut toutes les réservations
        // dont l'arrivée est <= t0 + temps_attente_min.
        // Le DÉPART du groupe = DERNIÈRE arrivée du groupe (sans ajouter l'attente).
        LinkedHashMap<Long, List<Reservation>> groupes = new LinkedHashMap<>();
        int idx = 0;
        while (idx < aTraiter.size()) {
            Reservation first = aTraiter.get(idx);
            long groupStart = first.getDate_heure_arrivee().getTime();
            long groupLimitArrival = groupStart + attenteMillis;

            List<Reservation> groupe = new ArrayList<>();
            groupe.add(first);
            idx++;

            while (idx < aTraiter.size()) {
                Reservation next = aTraiter.get(idx);
                if (next.getDate_heure_arrivee().getTime() <= groupLimitArrival) {
                    groupe.add(next);
                    idx++;
                } else {
                    break;
                }
            }

            // Le départ = la DERNIÈRE arrivée du groupe (sans ajouter le temps d'attente)
            long lastArrivalTime = groupe.get(groupe.size() - 1).getDate_heure_arrivee().getTime();
            long groupDepart = lastArrivalTime;
            groupes.put(groupDepart, groupe);
        }

        List<Reservation> reservationsNonAssignees = new ArrayList<>();

        for (Map.Entry<Long, List<Reservation>> entry : groupes.entrySet()) {
            Timestamp depart = new Timestamp(entry.getKey());
            List<Reservation> groupe = entry.getValue();
            // First-Fit Decreasing : trier par nb_passager décroissant
            groupe.sort((a, b) -> Integer.compare(b.getNb_passager(), a.getNb_passager()));

            // Calculer le retour du véhicule pour l'ensemble du groupe au même départ
            Timestamp retour;
            long l = 0;
            int id_hotel = hotel.getId_hotel(); // réinitialisé à l'aéroport pour chaque groupe
            try {
                groupe.sort((a, b) -> {
                    try {
                        return Double.compare(distanceService.getDistanceAeroportHotel(a.getId_hotel()),
                                distanceService.getDistanceAeroportHotel(b.getId_hotel()));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return 0;
                });

                for (Reservation reservation : groupe) {

                    Double distanceKm = distanceService.getDistanceValeur(id_hotel,
                            reservation.getId_hotel());
                    double dureeHeures = distanceKm / vitesseKmh;
                    double dureeMinutes = dureeHeures * 60;
                    long dureeMillis = (long) (dureeMinutes * 60 * 1000);
                    l += dureeMillis;
                    id_hotel = reservation.getId_hotel();
                }

                Double distanceKm = distanceService.getDistanceValeur(id_hotel,
                        hotel.getId_hotel());
                double dureeHeures = distanceKm / vitesseKmh;
                double dureeMinutes = dureeHeures * 60;
                long dureeMillis = (long) (dureeMinutes * 60 * 1000);
                l += dureeMillis;

                retour = new Timestamp(depart.getTime() + l);
            } catch (Exception e) {
                int refId = groupe.get(0).getId_reservation();
                System.err.println("Erreur calcul créneau pour réservation " + refId + ": " + e.getMessage());
                reservationsNonAssignees.addAll(groupe);
                continue;
            }
            groupe.sort((a, b) -> Integer.compare(b.getNb_passager(), a.getNb_passager()));

            // Bins de covoiturage : [idVehicule, placesRestantes]
            // Initialisé avec les véhicules déjà planifiés en DB sur ce créneau exact
            List<int[]> bins = new ArrayList<>();
            List<Integer> vehiculesUtilises = new ArrayList<>();

            // Charger les bins existants depuis la DB (appels précédents pour cette date)
            try {
                String sqlBins = "SELECT p.id_vehicule, v.place, COALESCE(SUM(r.nb_passager),0) as places_prises " +
                        "FROM Planification p " +
                        "JOIN Vehicule v ON v.id = p.id_vehicule " +
                        "JOIN Reservation r ON r.id_reservation = p.id_reservation " +
                        "WHERE p.date_heure_depart_aeroport = ? " +
                        "GROUP BY p.id_vehicule, v.place";
                try (Connection connBins = DatabaseConnection.getConnection();
                        PreparedStatement stmtBins = connBins.prepareStatement(sqlBins)) {
                    stmtBins.setTimestamp(1, depart);
                    try (ResultSet rsBins = stmtBins.executeQuery()) {
                        while (rsBins.next()) {
                            int idV = rsBins.getInt("id_vehicule");
                            int cap = rsBins.getInt("place");
                            int prises = rsBins.getInt("places_prises");
                            bins.add(new int[] { idV, cap - prises });
                            vehiculesUtilises.add(idV);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement bins existants: " + e.getMessage());
            }

            for (int groupeIdx = 0; groupeIdx < groupe.size(); groupeIdx++) {
                Reservation r = groupe.get(groupeIdx);
                int nbPax = r.getNb_passager();

                // Chercher le meilleur bin existant (best-fit : plus petit espace restant qui
                // suffit)
                int bestBinIdx = -1;
                int bestRemaining = Integer.MAX_VALUE;
                for (int i = 0; i < bins.size(); i++) {
                    int remaining = bins.get(i)[1];
                    if (remaining >= nbPax && remaining < bestRemaining) {
                        bestBinIdx = i;
                        bestRemaining = remaining;
                    }
                }

                if (bestBinIdx >= 0) {
                    // Covoiturage : place disponible dans un véhicule déjà ouvert
                    int covVehiculeId = bins.get(bestBinIdx)[0];
                    bins.get(bestBinIdx)[1] -= nbPax;
                    planifier(r.getId_reservation(), covVehiculeId, depart, retour);
                    // Recalculer le retour pour tout le covoiturage (route change)
                    recalculerRetourVehicule(covVehiculeId, depart, hotel, vitesseKmh);
                } else {
                    // Ouvrir un nouveau véhicule avec look-ahead :
                    // choisir le véhicule qui minimise le gaspillage après remplissage
                    // greedy avec les réservations restantes du groupe (triées ASC).
                    try {
                        List<Reservation> restantes = groupe.subList(groupeIdx + 1, groupe.size());
                        List<Vehicule> candidats = getAllVehiculesLibres(nbPax, depart, retour, vehiculesUtilises);
                        Vehicule vehicule = null;
                        int meilleurGaspillage = Integer.MAX_VALUE;
                        for (Vehicule candidat : candidats) {
                            int cap = candidat.getPlace() - nbPax;
                            // Greedy fill : trier restantes ASC et remplir autant que possible
                            List<Integer> paxRestants = new ArrayList<>();
                            for (Reservation rest : restantes) paxRestants.add(rest.getNb_passager());
                            Collections.sort(paxRestants);
                            int gaspillage = cap;
                            for (int pax : paxRestants) {
                                if (pax <= gaspillage) gaspillage -= pax;
                            }
                            if (gaspillage < meilleurGaspillage
                                    || (gaspillage == meilleurGaspillage && vehicule != null
                                            && candidat.getPlace() < vehicule.getPlace())) {
                                meilleurGaspillage = gaspillage;
                                vehicule = candidat;
                            }
                        }
                        if (vehicule != null) {
                            vehiculesUtilises.add(vehicule.getId());
                            bins.add(new int[] { vehicule.getId(), vehicule.getPlace() - nbPax });
                            planifier(r.getId_reservation(), vehicule.getId(), depart, retour);
                            // Recalculer le retour (même pour un seul → cohérence)
                            recalculerRetourVehicule(vehicule.getId(), depart, hotel, vitesseKmh);
                        } else {
                            reservationsNonAssignees.add(r);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur planification auto pour réservation "
                                + r.getId_reservation() + ": " + e.getMessage());
                        reservationsNonAssignees.add(r);
                    }
                }
            }
        }

        // Récupérer toutes les planifications de la date (anciennes + nouvelles)
        List<Planification> planifications = getPlanificationsByDate(date);

        Map<String, Object> result = new HashMap<>();
        result.put("planifications", planifications);
        result.put("reservationsNonAssignees", reservationsNonAssignees);
        return result;
    }
}
