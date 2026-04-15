package com.hotel.service.planification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Regroupement;
import com.hotel.model.Reservation;
import com.hotel.service.ParametreService;

public class RegroupementService {

    private final ParametreService parametreService;

    public RegroupementService() {
        this.parametreService = new ParametreService();
    }

    public RegroupementService(ParametreService parametreService) {
        this.parametreService = parametreService;
    }

    public Regroupement creerGroupeDynamique(Timestamp trigger, List<Reservation> nonAssignees) throws SQLException {
        return creerGroupeDynamique(trigger, nonAssignees, null);
    }

    public Regroupement creerGroupeDynamique(
            Timestamp trigger,
            List<Reservation> nonAssignees,
            Integer idVehiculeTrigger) throws SQLException {
        double tempsAttenteMin = parametreService.getValeurByCle("temps_attente_min", 30.0);
        long attenteMillis = (long) (tempsAttenteMin * 60 * 1000);

        Timestamp dateDebut = trigger;
        Timestamp dateFin = new Timestamp(trigger.getTime() + attenteMillis);

        for (Reservation reservation : nonAssignees) {
            Timestamp candidate = reservation.getDate_heure_depart_groupe() != null
                    ? reservation.getDate_heure_depart_groupe()
                    : reservation.getDate_heure_arrivee();
            if (candidate != null && candidate.after(dateFin)) {
                dateFin = candidate;
            }
        }

        return insererRegroupement(dateDebut, dateFin, "DYNAMIQUE", trigger, idVehiculeTrigger);
    }

    public Regroupement creerGroupeNormal(Timestamp dateDebut, Timestamp dateFin) throws SQLException {
        return insererRegroupement(dateDebut, dateFin, "NORMAL", null, null);
    }

    public List<Regroupement> getGroupesNormaux(java.util.Date date) throws SQLException {
        List<Regroupement> regroupements = chargerRegroupementsParDate(date);
        List<Regroupement> normaux = new ArrayList<>();
        for (Regroupement regroupement : regroupements) {
            if ("NORMAL".equalsIgnoreCase(regroupement.getType())) {
                normaux.add(regroupement);
            }
        }

        return normaux;
    }

    protected List<Regroupement> chargerRegroupementsParDate(java.util.Date date) throws SQLException {
        String sql = "SELECT id, date_debut, date_fin, type, date_trigger, id_vehicule_trigger " +
                "FROM Regroupement " +
                "WHERE DATE(date_debut) = ? " +
                "ORDER BY date_debut";

        List<Regroupement> regroupements = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(date.getTime()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    regroupements.add(mapperRegroupement(rs));
                }
            }
        }

        return regroupements;
    }

    protected Regroupement insererRegroupement(Timestamp dateDebut,
            Timestamp dateFin,
            String type,
            Timestamp dateTrigger,
            Integer idVehiculeTrigger) throws SQLException {
        String sql = "INSERT INTO Regroupement (date_debut, date_fin, type, date_trigger, id_vehicule_trigger) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id, date_debut, date_fin, type, date_trigger, id_vehicule_trigger";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, dateDebut);
            stmt.setTimestamp(2, dateFin);
            stmt.setString(3, type);
            stmt.setTimestamp(4, dateTrigger);
            if (idVehiculeTrigger != null) {
                stmt.setInt(5, idVehiculeTrigger);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapperRegroupement(rs);
                }
            }
        }

        throw new SQLException("Creation du regroupement impossible");
    }

    private Regroupement mapperRegroupement(ResultSet rs) throws SQLException {
        Regroupement regroupement = new Regroupement();
        regroupement.setId(rs.getInt("id"));
        regroupement.setDateDebut(rs.getTimestamp("date_debut"));
        regroupement.setDateFin(rs.getTimestamp("date_fin"));
        regroupement.setType(rs.getString("type"));
        regroupement.setDateTrigger(rs.getTimestamp("date_trigger"));
        int idVehiculeTrigger = rs.getInt("id_vehicule_trigger");
        regroupement.setIdVehiculeTrigger(rs.wasNull() ? null : idVehiculeTrigger);
        return regroupement;
    }
}
