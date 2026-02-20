package com.hotel.service;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Planification;
import com.hotel.model.Reservation;
import com.hotel.model.Vehicule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanificationService {

    private ParametreService parametreService = new ParametreService();
    private DistanceService distanceService = new DistanceService();
    private VehiculeService vehiculeService = new VehiculeService();
    private ReservationService reservationService = new ReservationService();

    /**
     * Vérifie si un véhicule est disponible sur un créneau donné
     * Un véhicule est disponible si aucune planification existante ne chevauche le créneau
     * @param idVehicule ID du véhicule
     * @param dateHeureDepart Début du créneau
     * @param dateHeureRetour Fin du créneau
     * @return true si le véhicule est disponible
     */
    public boolean estVoitureDisponible(int idVehicule, Timestamp dateHeureDepart, Timestamp dateHeureRetour) throws SQLException {
        // Recherche de chevauchement : 
        // Un créneau existant chevauche si : existing.depart < new.retour AND existing.retour > new.depart
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
     * Calcule l'heure de départ depuis l'aéroport (avant l'arrivée du vol)
     * date_heure_depart_aeroport = date_heure_arrivee_vol - duree_trajet
     * @param idReservation ID de la réservation
     * @return Le timestamp de départ de l'aéroport
     */
    public Timestamp getDateHeureDepartAeroport(int idReservation) throws SQLException {
        Reservation reservation = getReservationById(idReservation);
        if (reservation == null) {
            throw new SQLException("Réservation non trouvée: " + idReservation);
        }

        double dureeMinutes = getDureeTrajetMinutes(idReservation);
        long dureeMillis = (long) (dureeMinutes * 60 * 1000);

        // Départ = arrivée vol - durée trajet
        long departMillis = reservation.getDate_heure_arrivee().getTime() - dureeMillis;
        return new Timestamp(departMillis);
    }

    /**
     * Calcule l'heure de retour à l'aéroport (après dépôt au hotel)
     * date_heure_retour_aeroport = date_heure_arrivee_vol + duree_trajet
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
        long retourMillis = reservation.getDate_heure_arrivee().getTime() + dureeMillis;
        return new Timestamp(retourMillis);
    }

    /**
     * Sélectionne le véhicule le plus approprié pour une réservation selon les règles de gestion:
     * 1. nb_passager <= place du véhicule
     * 2. Choisir celui qui laisse le moins de places vides
     * 3. Si égalité → priorité au Diesel ('D')
     * 4. Si égalité de capacité ET de type → random
     * 5. Le véhicule doit être disponible sur le créneau
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
        //           2) type_carburant = 'D' DESC (Diesel en priorité)
        //           3) RANDOM() pour départager
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
     * @param idReservation ID de la réservation
     * @param idVehicule ID du véhicule
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
     * Récupère toutes les planifications d'une date donnée
     * @param date La date pour laquelle récupérer les planifications
     * @return Liste des planifications avec infos jointes (client, hotel, vehicule)
     */
    public List<Planification> getPlanificationsByDate(java.util.Date date) throws SQLException {
        List<Planification> planifications = new ArrayList<>();
        
        String sql = "SELECT p.*, r.id_client, h.nom as nom_hotel, v.reference as reference_vehicule " +
                     "FROM Planification p " +
                     "JOIN Reservation r ON p.id_reservation = r.id_reservation " +
                     "JOIN Hotel h ON r.id_hotel = h.id_hotel " +
                     "JOIN Vehicule v ON p.id_vehicule = v.id " +
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
                    planification.setNomHotel(rs.getString("nom_hotel"));
                    planification.setReferenceVehicule(rs.getString("reference_vehicule"));
                    planifications.add(planification);
                }
            }
        }

        return planifications;
    }

    /**
     * Récupère les réservations sans planification associée
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
}
