package com.hotel.service;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class ReservationService {
    
    public Reservation reserver(String id_client, int nb_passager, 
                               Timestamp date_heure_arrivee, int id_hotel) throws SQLException {
        String sql = "INSERT INTO Reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) " +
                     "VALUES (?, ?, ?, ?) RETURNING id_reservation";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id_client);
            stmt.setInt(2, nb_passager);
            stmt.setTimestamp(3, date_heure_arrivee);
            stmt.setInt(4, id_hotel);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId_reservation(rs.getInt("id_reservation"));
                reservation.setId_client(id_client);
                reservation.setNb_passager(nb_passager);
                reservation.setDate_heure_arrivee(date_heure_arrivee);
                reservation.setId_hotel(id_hotel);
                return reservation;
            }
        }
        
        return null;
    }
    
    public List<Reservation> getAllReservation() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as nom_hotel FROM Reservation r " +
                     "LEFT JOIN Hotel h ON r.id_hotel = h.id_hotel " +
                     "ORDER BY r.date_heure_arrivee DESC";
        
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
    
    public List<Reservation> getReservationByDate(Date date) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as nom_hotel FROM Reservation r " +
                     "LEFT JOIN Hotel h ON r.id_hotel = h.id_hotel " +
                     "WHERE DATE(r.date_heure_arrivee) = ? " +
                     "ORDER BY r.date_heure_arrivee";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, new java.sql.Date(date.getTime()));
            
            try (ResultSet rs = stmt.executeQuery()) {
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
        }
        
        return reservations;
    }
}
