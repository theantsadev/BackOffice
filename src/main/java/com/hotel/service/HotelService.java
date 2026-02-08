package com.hotel.service;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Hotel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
}
