package com.hotel.service.planification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Reservation;
import com.hotel.model.Vehicule;

public class VehiculeSelectionService {

    private final int sprint6TieBreakerSeed;

    public VehiculeSelectionService() {
        this.sprint6TieBreakerSeed = (int) (System.nanoTime() & 0x7fffffff);
    }

    public boolean estVoitureDisponible(int idVehicule, Timestamp dateHeureDepart, Timestamp dateHeureRetour)
            throws SQLException {
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

    public Vehicule getVehiculeApproprieForReservation(int nbPassagers, Timestamp depart, Timestamp retour)
            throws SQLException {
        String sql = "SELECT * FROM Vehicule WHERE place >= ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nbPassagers);
            List<Vehicule> candidatsDisponibles = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id"));
                    vehicule.setReference(rs.getString("reference"));
                    vehicule.setPlace(rs.getInt("place"));
                    vehicule.setTypeCarburant(rs.getString("type_carburant"));

                    if (estVoitureDisponible(vehicule.getId(), depart, retour)) {
                        candidatsDisponibles.add(vehicule);
                    }
                }
            }

            if (!candidatsDisponibles.isEmpty()) {
                Map<Integer, Integer> nbTrajetsByVehicule = chargerNombreTrajetsParVehicule(
                        extraireIdsVehicules(candidatsDisponibles));
                candidatsDisponibles.sort(
                        (a, b) -> comparerVehiculesSprint6(a, b, nbPassagers, nbTrajetsByVehicule));
                return candidatsDisponibles.get(0);
            }
        }

        return null;
    }

    public List<Vehicule> getAllVehiculesLibres(int nbPax, Timestamp depart, Timestamp retour,
            List<Integer> excludeIds) throws SQLException {
        List<Vehicule> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Vehicule WHERE place >= ?");
        if (!excludeIds.isEmpty()) {
            sql.append(" AND id NOT IN (");
            for (int i = 0; i < excludeIds.size(); i++)
                sql.append(i == 0 ? "?" : ",?");
            sql.append(")");
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setInt(idx++, nbPax);
            for (int id : excludeIds)
                stmt.setInt(idx++, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule v = new Vehicule();
                    v.setId(rs.getInt("id"));
                    v.setReference(rs.getString("reference"));
                    v.setPlace(rs.getInt("place"));
                    v.setTypeCarburant(rs.getString("type_carburant"));
                    if (estVoitureDisponible(v.getId(), depart, retour))
                        result.add(v);
                }
            }
        }

        if (!result.isEmpty()) {
            Map<Integer, Integer> nbTrajetsByVehicule = chargerNombreTrajetsParVehicule(extraireIdsVehicules(result));
            result.sort((a, b) -> comparerVehiculesSprint6(a, b, nbPax, nbTrajetsByVehicule));
        }

        return result;
    }

    public Vehicule choisirVehiculeAvecLookAhead(int nbPax,
            Timestamp depart,
            Timestamp retour,
            List<Integer> vehiculesUtilises,
            List<Reservation> restantes) throws SQLException {
        List<Vehicule> candidats = getAllVehiculesLibres(1, depart, retour, vehiculesUtilises);
        Vehicule meilleurVehicule = null;
        int meilleurGaspillage = Integer.MAX_VALUE;
        Map<Integer, Integer> nbTrajetsByVehicule = chargerNombreTrajetsParVehicule(extraireIdsVehicules(candidats));

        for (Vehicule candidat : candidats) {
            int passagersAffectes = Math.min(nbPax, candidat.getPlace());
            int placesRestantes = candidat.getPlace() - passagersAffectes;
            int gaspillage = evaluerGaspillage(placesRestantes, restantes);
            if (gaspillage < meilleurGaspillage
                    || (gaspillage == meilleurGaspillage && meilleurVehicule != null
                            && comparerVehiculesSprint6(candidat, meilleurVehicule, passagersAffectes,
                                    nbTrajetsByVehicule) < 0)) {
                meilleurGaspillage = gaspillage;
                meilleurVehicule = candidat;
            } else if (gaspillage == meilleurGaspillage && meilleurVehicule == null) {
                meilleurVehicule = candidat;
            }
        }

        return meilleurVehicule;
    }

    public boolean doitPrendreNouveauVehicule(VehiculeBin binCandidat,
            Vehicule nouveauVehicule,
            int passagersRestants) throws SQLException {
        int passagersAffectesBin = Math.min(passagersRestants, binCandidat.getPlacesRestantes());
        int passagersAffectesNouveau = Math.min(passagersRestants, nouveauVehicule.getPlace());

        if (passagersAffectesNouveau > passagersAffectesBin) {
            return true;
        }
        if (passagersAffectesNouveau < passagersAffectesBin) {
            return false;
        }

        int resteBinApresAssignation = binCandidat.getPlacesRestantes() - passagersAffectesBin;
        int resteNouveauApresAssignation = nouveauVehicule.getPlace() - passagersAffectesNouveau;

        if (resteNouveauApresAssignation < resteBinApresAssignation) {
            return true;
        }
        if (resteNouveauApresAssignation > resteBinApresAssignation) {
            return false;
        }

        Vehicule vehiculeVirtuelBin = new Vehicule();
        vehiculeVirtuelBin.setId(binCandidat.getIdVehicule());
        vehiculeVirtuelBin.setPlace(binCandidat.getPlacesRestantes());
        vehiculeVirtuelBin.setTypeCarburant(binCandidat.getTypeCarburant());

        List<Integer> ids = new ArrayList<>();
        ids.add(binCandidat.getIdVehicule());
        ids.add(nouveauVehicule.getId());
        Map<Integer, Integer> nbTrajetsByVehicule = chargerNombreTrajetsParVehicule(ids);
        int passagersComparaison = Math.min(passagersRestants,
                Math.min(binCandidat.getPlacesRestantes(), nouveauVehicule.getPlace()));
        int comparaison = comparerVehiculesSprint6(
                nouveauVehicule,
                vehiculeVirtuelBin,
                passagersComparaison,
                nbTrajetsByVehicule);
        return comparaison < 0;
    }

    public int trouverMeilleurBin(List<VehiculeBin> bins, int nbPassagers) {
        int bestIdx = -1;
        int bestReste = Integer.MAX_VALUE;

        for (int i = 0; i < bins.size(); i++) {
            int reste = bins.get(i).getPlacesRestantes();
            int passagersAffectes = Math.min(reste, nbPassagers);
            if (passagersAffectes <= 0) {
                continue;
            }
            int resteApresAssignation = reste - passagersAffectes;
            if (resteApresAssignation < bestReste) {
                bestReste = resteApresAssignation;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    public Map<Integer, Integer> chargerNombreTrajetsParVehicule(List<Integer> vehiculeIds) throws SQLException {
        Map<Integer, Integer> nbTrajetsByVehicule = new HashMap<>();
        if (vehiculeIds.isEmpty()) {
            return nbTrajetsByVehicule;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT id_vehicule, COUNT(DISTINCT date_heure_depart_aeroport) AS nb_trajets FROM Planification WHERE id_vehicule IN (");
        for (int i = 0; i < vehiculeIds.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(") GROUP BY id_vehicule");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Integer vehiculeId : vehiculeIds) {
                stmt.setInt(idx++, vehiculeId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    nbTrajetsByVehicule.put(rs.getInt("id_vehicule"), rs.getInt("nb_trajets"));
                }
            }
        }

        return nbTrajetsByVehicule;
    }

    public int comparerVehiculesSprint6(Vehicule a,
            Vehicule b,
            int nbPassagers,
            Map<Integer, Integer> nbTrajetsByVehicule) {
        int videA = a.getPlace() - nbPassagers;
        int videB = b.getPlace() - nbPassagers;
        int cmpCapacite = Integer.compare(videA, videB);
        if (cmpCapacite != 0) {
            return cmpCapacite;
        }

        int trajetsA = nbTrajetsByVehicule.getOrDefault(a.getId(), 0);
        int trajetsB = nbTrajetsByVehicule.getOrDefault(b.getId(), 0);
        int cmpTrajets = Integer.compare(trajetsA, trajetsB);
        if (cmpTrajets != 0) {
            return cmpTrajets;
        }

        boolean dieselA = "D".equalsIgnoreCase(a.getTypeCarburant());
        boolean dieselB = "D".equalsIgnoreCase(b.getTypeCarburant());
        if (dieselA != dieselB) {
            return dieselA ? -1 : 1;
        }

        int tieA = melangerPourTieBreak(a.getId());
        int tieB = melangerPourTieBreak(b.getId());
        int cmpRandomLike = Integer.compare(tieA, tieB);
        if (cmpRandomLike != 0) {
            return cmpRandomLike;
        }

        return Integer.compare(a.getId(), b.getId());
    }

    private int melangerPourTieBreak(int vehiculeId) {
        int x = vehiculeId ^ sprint6TieBreakerSeed;
        x ^= (x << 13);
        x ^= (x >>> 17);
        x ^= (x << 5);
        return x & 0x7fffffff;
    }

    private int evaluerGaspillage(int placesRestantes, List<Reservation> restantes) {
        List<Integer> paxRestants = new ArrayList<>();
        for (Reservation reservation : restantes) {
            paxRestants.add(reservation.getNb_passager());
        }

        Collections.sort(paxRestants);
        int gaspillage = placesRestantes;
        for (int pax : paxRestants) {
            if (pax <= gaspillage) {
                gaspillage -= pax;
            }
        }
        return gaspillage;
    }

    public List<Integer> extraireIdsVehicules(List<Vehicule> vehicules) {
        List<Integer> ids = new ArrayList<>();
        for (Vehicule vehicule : vehicules) {
            ids.add(vehicule.getId());
        }
        return ids;
    }
}
