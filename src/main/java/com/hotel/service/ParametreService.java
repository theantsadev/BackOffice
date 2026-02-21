package com.hotel.service;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Parametre;

import java.sql.*;

public class ParametreService {

    /**
     * Récupère un paramètre par sa clé
     * @param cle La clé du paramètre (ex: "vitesse_moyenne_kmh")
     * @return Le paramètre ou null si non trouvé
     */
    public Parametre getParametreByCle(String cle) throws SQLException {
        String sql = "SELECT * FROM Parametre WHERE cle = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cle);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Parametre parametre = new Parametre();
                    parametre.setIdParametre(rs.getInt("id_parametre"));
                    parametre.setCle(rs.getString("cle"));
                    parametre.setValeur(rs.getDouble("valeur"));
                    parametre.setUnite(rs.getString("unite"));
                    return parametre;
                }
            }
        }

        return null;
    }

    /**
     * Récupère la valeur d'un paramètre par sa clé
     * @param cle La clé du paramètre
     * @param defaultValue Valeur par défaut si le paramètre n'existe pas
     * @return La valeur du paramètre
     */
    public double getValeurByCle(String cle, double defaultValue) throws SQLException {
        Parametre parametre = getParametreByCle(cle);
        return parametre != null ? parametre.getValeur() : defaultValue;
    }
}
