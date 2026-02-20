package com.hotel.service;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Distance;

import java.sql.*;

public class DistanceService {

    /**
     * Récupère la distance entre deux hôtels (recherche bidirectionnelle)
     * La distance A→B = B→A. En base, on stocke uniquement from_hotel < to_hotel
     * @param fromHotelId ID de l'hôtel de départ
     * @param toHotelId ID de l'hôtel d'arrivée
     * @return La distance ou null si non trouvée
     */
    public Distance getDistanceByFromAndTo(int fromHotelId, int toHotelId) throws SQLException {
        // Recherche dans les deux sens (from/to ou to/from)
        String sql = "SELECT * FROM Distance WHERE " +
                     "(from_hotel = ? AND to_hotel = ?) OR (from_hotel = ? AND to_hotel = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fromHotelId);
            stmt.setInt(2, toHotelId);
            stmt.setInt(3, toHotelId);
            stmt.setInt(4, fromHotelId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Distance distance = new Distance();
                    distance.setIdDistance(rs.getInt("id_distance"));
                    distance.setFromHotel(rs.getInt("from_hotel"));
                    distance.setToHotel(rs.getInt("to_hotel"));
                    distance.setValeur(rs.getDouble("valeur"));
                    return distance;
                }
            }
        }

        return null;
    }

    /**
     * Récupère la valeur de distance entre deux hôtels
     * @param fromHotelId ID de l'hôtel de départ
     * @param toHotelId ID de l'hôtel d'arrivée
     * @return La valeur de la distance en km, ou -1 si non trouvée
     */
    public double getDistanceValeur(int fromHotelId, int toHotelId) throws SQLException {
        Distance distance = getDistanceByFromAndTo(fromHotelId, toHotelId);
        return distance != null ? distance.getValeur() : -1;
    }

    /**
     * Récupère la distance entre l'aéroport (id=0) et un hôtel
     * @param hotelId ID de l'hôtel
     * @return La distance en km
     */
    public double getDistanceAeroportHotel(int hotelId) throws SQLException {
        return getDistanceValeur(0, hotelId);
    }
}
