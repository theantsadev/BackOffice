package com.hotel.service.planification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.hotel.database.DatabaseConnection;
import com.hotel.service.ParametreService;

public class ChevauchementService {

    private final ParametreService parametreService;

    public ChevauchementService() {
        this.parametreService = new ParametreService();
    }

    public ChevauchementService(ParametreService parametreService) {
        this.parametreService = parametreService;
    }

    public boolean chevaucheGroupeNormal(Timestamp debut, Timestamp fin, java.util.Date date) throws SQLException {
        double tempsAttenteMin = parametreService.getValeurByCle("temps_attente_min", 30.0);
        long attenteMillis = (long) (tempsAttenteMin * 60 * 1000);

        Timestamp borneDebut = new Timestamp(debut.getTime() - attenteMillis);
        Timestamp borneFin = new Timestamp(fin.getTime() + attenteMillis);

        return existeDepartNormalDansFenetre(borneDebut, borneFin, date);
    }

    protected boolean existeDepartNormalDansFenetre(Timestamp borneDebut, Timestamp borneFin, java.util.Date date)
            throws SQLException {
        String sql = "SELECT 1 FROM Planification " +
                "WHERE is_dynamique = FALSE " +
                "AND DATE(date_heure_depart_aeroport) = ? " +
                "AND date_heure_depart_aeroport BETWEEN ? AND ? " +
                "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(date.getTime()));
            stmt.setTimestamp(2, borneDebut);
            stmt.setTimestamp(3, borneFin);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
