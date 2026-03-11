package com.hotel.service;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Hotel;
import com.hotel.model.Parametre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.SliderUI;

public class HotelService {

    public List<Hotel> getAllHotel() throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM Hotel ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Hotel hotel = new Hotel();
                hotel.setId_hotel(rs.getInt("id_hotel"));
                hotel.setNom(rs.getString("nom"));
                hotels.add(hotel);
            }
        }

        return hotels;
    }

    public Hotel getById(int id_hotel) throws SQLException {
        String sql = "SELECT * FROM Hotel WHERE id_hotel = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {

            stmt.setInt(1, id_hotel);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Hotel hotel = new Hotel();
                    hotel.setId_hotel(rs.getInt("id_hotel"));
                    hotel.setNom(rs.getString("nom"));
                    return hotel;
                }
            }
        }
        return null;

    }

    public Hotel getAeroport() throws SQLException {
        return getById(0);
    }
}
