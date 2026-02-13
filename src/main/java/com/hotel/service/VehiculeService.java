package com.hotel.service;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Vehicule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeService {

    public List<Vehicule> getAllVehicules() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM Vehicule ORDER BY reference";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Vehicule vehicule = new Vehicule();
                vehicule.setId(rs.getInt("id"));
                vehicule.setReference(rs.getString("reference"));
                vehicule.setPlace(rs.getInt("place"));
                vehicule.setTypeCarburant(rs.getString("type_carburant"));
                vehicules.add(vehicule);
            }
        }

        return vehicules;
    }

    public Vehicule getVehiculeById(int id) throws SQLException {
        String sql = "SELECT * FROM Vehicule WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id"));
                    vehicule.setReference(rs.getString("reference"));
                    vehicule.setPlace(rs.getInt("place"));
                    vehicule.setTypeCarburant(rs.getString("type_carburant"));
                    return vehicule;
                }
            }
        }

        return null;
    }

    public Vehicule createVehicule(String reference, int place, String typeCarburant) throws SQLException {
        String sql = "INSERT INTO Vehicule (reference, place, type_carburant) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reference);
            stmt.setInt(2, place);
            stmt.setString(3, typeCarburant);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Vehicule vehicule = new Vehicule();
                vehicule.setId(rs.getInt("id"));
                vehicule.setReference(reference);
                vehicule.setPlace(place);
                vehicule.setTypeCarburant(typeCarburant);
                return vehicule;
            }
        }

        return null;
    }

    public Vehicule updateVehicule(int id, String reference, int place, String typeCarburant) throws SQLException {
        String sql = "UPDATE Vehicule SET reference = ?, place = ?, type_carburant = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reference);
            stmt.setInt(2, place);
            stmt.setString(3, typeCarburant);
            stmt.setInt(4, id);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                Vehicule vehicule = new Vehicule();
                vehicule.setId(id);
                vehicule.setReference(reference);
                vehicule.setPlace(place);
                vehicule.setTypeCarburant(typeCarburant);
                return vehicule;
            }
        }

        return null;
    }

    public boolean deleteVehicule(int id) throws SQLException {
        String sql = "DELETE FROM Vehicule WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }
}
