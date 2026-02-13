package com.hotel.service;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Token;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TokenService {

    public List<Token> getAllTokens() throws SQLException {
        List<Token> tokens = new ArrayList<>();
        String sql = "SELECT * FROM Token ORDER BY date_heure_expiration DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Token token = new Token();
                token.setId(rs.getInt("id"));
                token.setToken(rs.getString("token"));
                token.setDateHeureExpiration(rs.getTimestamp("date_heure_expiration"));
                tokens.add(token);
            }
        }

        return tokens;
    }

    public Token getTokenById(int id) throws SQLException {
        String sql = "SELECT * FROM Token WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Token token = new Token();
                    token.setId(rs.getInt("id"));
                    token.setToken(rs.getString("token"));
                    token.setDateHeureExpiration(rs.getTimestamp("date_heure_expiration"));
                    return token;
                }
            }
        }

        return null;
    }

    public Token getTokenByValue(String tokenValue) throws SQLException {
        String sql = "SELECT * FROM Token WHERE token = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tokenValue);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Token token = new Token();
                    token.setId(rs.getInt("id"));
                    token.setToken(rs.getString("token"));
                    token.setDateHeureExpiration(rs.getTimestamp("date_heure_expiration"));
                    return token;
                }
            }
        }

        return null;
    }

    public Token createToken(String tokenValue, Timestamp dateHeureExpiration) throws SQLException {
        String sql = "INSERT INTO Token (token, date_heure_expiration) VALUES (?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tokenValue);
            stmt.setTimestamp(2, dateHeureExpiration);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Token token = new Token();
                token.setId(rs.getInt("id"));
                token.setToken(tokenValue);
                token.setDateHeureExpiration(dateHeureExpiration);
                return token;
            }
        }

        return null;
    }

    public Token generateToken(int expirationMinutes) throws SQLException {
        String tokenValue = UUID.randomUUID().toString();
        Timestamp expiration = new Timestamp(System.currentTimeMillis() + (expirationMinutes * 60 * 1000L));
        return createToken(tokenValue, expiration);
    }

    public boolean deleteToken(int id) throws SQLException {
        String sql = "DELETE FROM Token WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    public int deleteExpiredTokens() throws SQLException {
        String sql = "DELETE FROM Token WHERE date_heure_expiration < NOW()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return stmt.executeUpdate();
        }
    }

    public boolean isTokenValid(String tokenValue) throws SQLException {
        Token token = getTokenByValue(tokenValue);
        return token != null && !token.isExpired();
    }
}
