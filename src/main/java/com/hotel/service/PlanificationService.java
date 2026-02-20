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
}
