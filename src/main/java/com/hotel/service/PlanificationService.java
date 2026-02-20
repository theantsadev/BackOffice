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
}
