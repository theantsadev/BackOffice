package com.hotel.service.planification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotel.database.DatabaseConnection;
import com.hotel.model.Vehicule;

public class VehiculeSelectionService {

    private final int sprint6TieBreakerSeed;

    public VehiculeSelectionService() {
        this.sprint6TieBreakerSeed = (int) (System.nanoTime() & 0x7fffffff);
    }

    public boolean estVoitureDisponible(int idVehicule, Timestamp dateHeureDepart, Timestamp dateHeureRetour)
            throws SQLException {

        // 1. Vérifier les conflits de planification
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
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
        }

        Time heureDepart = new Time(dateHeureDepart.getTime());

        String sqlTestDispo = "SELECT COUNT(*) FROM Vehicule " +
                "WHERE id = ? " +
                "AND heure_debut_dispo <= ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlTestDispo)) {

            stmt.setInt(1, idVehicule);
            stmt.setTime(2, heureDepart); // ✅ CORRECTION

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    return false;
                }
            }
        }

        return true; // ✅ disponible
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

    /**
     * Récupère TOUS les véhicules disponibles pour la scission (sans filtre sur
     * nbPax).
     * Utilisé quand aucun véhicule ne peut prendre tous les passagers.
     * Retourne les véhicules triés par nombre de places DÉCROISSANT.
     *
     * @param depart     date/heure de départ
     * @param retour     date/heure de retour
     * @param excludeIds IDs des véhicules à exclure
     * @return liste des véhicules disponibles triés par places décroissantes
     */
    public List<Vehicule> getAllVehiculesLibresPourScission(Timestamp depart, Timestamp retour,
            List<Integer> excludeIds) throws SQLException {
        List<Vehicule> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Vehicule WHERE place > 0");
        if (!excludeIds.isEmpty()) {
            sql.append(" AND id NOT IN (");
            for (int i = 0; i < excludeIds.size(); i++)
                sql.append(i == 0 ? "?" : ",?");
            sql.append(")");
        }
        sql.append(" ORDER BY place DESC"); // Tri par places décroissantes

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
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

        return result;
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

    public List<Integer> extraireIdsVehicules(List<Vehicule> vehicules) {
        List<Integer> ids = new ArrayList<>();
        for (Vehicule vehicule : vehicules) {
            ids.add(vehicule.getId());
        }
        return ids;
    }

    // =====================================================================
    // SPRINT 7 : Algorithme Best-Fit avec considération du retour véhicule
    // =====================================================================

    /**
     * Trouve la prochaine heure à laquelle un véhicule sera disponible.
     *
     * Si aucun véhicule n'est disponible à l'heure de départ demandée,
     * cette méthode trouve l'heure de la prochaine disponibilité.
     *
     * @param dateDepart      date/heure de départ souhaitée
     * @param nbPaxMin        nombre minimum de places requis (0 pour ignorer)
     * @param vehiculesExclus liste des IDs de véhicules à exclure
     * @return le Timestamp ajusté avec la prochaine heure de disponibilité, ou null
     * @throws SQLException en cas d'erreur d'accès à la base de données
     */
    public Timestamp trouverProchaineDisponibilite(Timestamp dateDepart, int nbPaxMin,
            List<Integer> vehiculesExclus) throws SQLException {

        // Extraire l'heure du départ demandé
        Time heureDepart = new Time(dateDepart.getTime());

        // Requête SQL pour trouver le véhicule avec la plus petite heure_debut_dispo >
        // heureDepart
        // Note: on utilise > et non >= pour trouver la PROCHAINE dispo (pas l'actuelle)
        StringBuilder sql = new StringBuilder(
                "SELECT MIN(heure_debut_dispo) as prochaine_dispo " +
                        "FROM Vehicule " +
                        "WHERE heure_debut_dispo > ? ");

        // Filtre optionnel sur le nombre de places (0 = ignorer pour scission)
        if (nbPaxMin > 0) {
            sql.append("AND place >= ? ");
        }

        if (!vehiculesExclus.isEmpty()) {
            sql.append("AND id NOT IN (");
            for (int i = 0; i < vehiculesExclus.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
            }
            sql.append(") ");
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            stmt.setTime(idx++, heureDepart);

            if (nbPaxMin > 0) {
                stmt.setInt(idx++, nbPaxMin);
            }

            for (Integer idExclu : vehiculesExclus) {
                stmt.setInt(idx++, idExclu);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Time prochaineDispo = rs.getTime("prochaine_dispo");
                    if (prochaineDispo != null) {
                        // Construire un nouveau Timestamp avec la même date mais l'heure ajustée
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTimeInMillis(dateDepart.getTime());

                        java.util.Calendar calHeure = java.util.Calendar.getInstance();
                        calHeure.setTime(prochaineDispo);

                        cal.set(java.util.Calendar.HOUR_OF_DAY, calHeure.get(java.util.Calendar.HOUR_OF_DAY));
                        cal.set(java.util.Calendar.MINUTE, calHeure.get(java.util.Calendar.MINUTE));
                        cal.set(java.util.Calendar.SECOND, calHeure.get(java.util.Calendar.SECOND));

                        return new Timestamp(cal.getTimeInMillis());
                    }
                }
            }
        }

        return null; // Aucun véhicule disponible
    }

    /**
     * Trouve le premier véhicule disponible à partir d'une heure donnée (Best-Fit).
     *
     * Cette méthode considère l'heure de disponibilité des véhicules
     * (heure_debut_dispo)
     * et peut retarder le départ si nécessaire.
     *
     * GÈRE LA SCISSION : si aucun véhicule n'a assez de places, retourne le plus
     * grand.
     *
     * @param nbPax           nombre de passagers à transporter
     * @param dateDepart      date/heure de départ souhaitée (sera ajustée si
     *                        nécessaire)
     * @param retour          date/heure de retour estimée
     * @param vehiculesExclus liste des IDs de véhicules déjà utilisés
     * @return VehiculeRetour contenant le véhicule et la date de départ effective,
     *         ou null
     * @throws SQLException en cas d'erreur d'accès à la base de données
     */
    public VehiculeRetour trouverVehiculeBestFitAvecAttente(int nbPax, Timestamp dateDepart,
            Timestamp retour, List<Integer> vehiculesExclus, long attenteMaxMillis)
            throws SQLException {

        // 1. Essayer de trouver un véhicule disponible immédiatement
        Vehicule vehiculeImmediat = trouverVehiculeBestFit(nbPax, dateDepart, retour, vehiculesExclus);

        // 2. Vérifier si un véhicule en retour proche est un meilleur choix.
        VehiculeRetour vehiculeEnRetour = trouverVehiculeEnRetour(
                nbPax,
                dateDepart,
                attenteMaxMillis,
                vehiculesExclus);

        if (vehiculeEnRetourEstMeilleur(vehiculeImmediat, vehiculeEnRetour, nbPax, dateDepart)) {
            return vehiculeEnRetour;
        }

        if (vehiculeImmediat != null) {
            return new VehiculeRetour(vehiculeImmediat, dateDepart);
        }

        if (vehiculeEnRetour != null) {
            return vehiculeEnRetour;
        }

        // 3. Sinon, trouver la prochaine heure de disponibilité
        // Note: on passe 0 pour nbPaxMin car on gère la scission dans
        // trouverVehiculeBestFit
        Timestamp prochaineDispo = trouverProchaineDisponibilite(dateDepart, 0, vehiculesExclus);
        if (prochaineDispo == null) {
            return null; // Aucun véhicule disponible
        }

        // 4. Calculer le nouveau retour basé sur la prochaine dispo
        long dureeTrajet = retour.getTime() - dateDepart.getTime();
        Timestamp nouveauRetour = new Timestamp(prochaineDispo.getTime() + dureeTrajet);

        // 5. Trouver le meilleur véhicule à cette nouvelle heure
        Vehicule vehicule = trouverVehiculeBestFit(nbPax, prochaineDispo, nouveauRetour, vehiculesExclus);
        if (vehicule != null) {
            return new VehiculeRetour(vehicule, prochaineDispo);
        }

        return null;
    }

    /**
     * Trouve le véhicule disponible avec le MOINS de gaspillage (stratégie
     * Best-Fit).
     *
     * L'algorithme Best-Fit sélectionne le véhicule dont la capacité est la plus
     * proche du nombre de passagers, afin de minimiser les places vides
     * (gaspillage).
     *
     * IMPORTANT pour la SCISSION :
     * Si aucun véhicule n'a assez de places pour tous les passagers,
     * on retourne le véhicule avec le MAXIMUM de places pour en prendre le plus
     * possible.
     *
     * Exemple :
     * - 20 passagers à transporter
     * - Véhicule A : 12 places, Véhicule B : 9 places
     * - Aucun ne peut tout prendre → on choisit A (12 places) pour scinder
     *
     * @param nbPax           nombre de passagers à transporter
     * @param depart          date/heure de départ souhaitée
     * @param retour          date/heure de retour estimée
     * @param vehiculesExclus liste des IDs de véhicules déjà utilisés (à exclure)
     * @return le véhicule optimal (Best-Fit), ou null si aucun véhicule disponible
     * @throws SQLException en cas d'erreur d'accès à la base de données
     */
    public Vehicule trouverVehiculeBestFit(int nbPax, Timestamp depart, Timestamp retour,
            List<Integer> vehiculesExclus) throws SQLException {

        // 1. Essayer de trouver un véhicule avec assez de places (Best-Fit classique)
        List<Vehicule> candidats = getAllVehiculesLibres(nbPax, depart, retour, vehiculesExclus);

        if (!candidats.isEmpty()) {
            // Best-Fit : trouver le véhicule avec le moins de gaspillage
            Vehicule meilleur = null;
            int gaspillageMin = Integer.MAX_VALUE;

            for (Vehicule candidat : candidats) {
                int gaspillage = candidat.getPlace() - nbPax;
                if (gaspillage < gaspillageMin) {
                    gaspillageMin = gaspillage;
                    meilleur = candidat;
                }
            }
            return meilleur;
        }

        // 2. SCISSION : aucun véhicule n'a assez de places
        // → Prendre le véhicule avec le MAXIMUM de places
        List<Vehicule> tousVehicules = getAllVehiculesLibresPourScission(depart, retour, vehiculesExclus);

        if (!tousVehicules.isEmpty()) {
            // Le premier a le max de places (trié DESC)
            return tousVehicules.get(0);
        }

        return null; // Aucun véhicule disponible
    }

    /**
     * Cherche un véhicule EN COURS DE TRAJET qui sera bientôt disponible (après son
     * retour).
     *
     * Cette méthode permet de considérer les véhicules qui ne sont pas disponibles
     * immédiatement mais qui le seront après leur retour à l'aéroport.
     *
     * Cas d'utilisation :
     * - Un groupe doit partir à 10h30
     * - Le véhicule V1 (7 places) est en trajet et revient à 11h00
     * - Le véhicule V2 (9 places) est disponible immédiatement
     * - Si V1 est meilleur (Best-Fit), on peut décider d'attendre son retour
     * - La date de départ du groupe devient alors 11h00
     *
     * @param nbPax           nombre de passagers minimum que le véhicule doit
     *                        pouvoir transporter
     * @param departSouhaite  date/heure de départ souhaitée (on cherche les retours
     *                        après cette heure)
     * @param delaiMaxMillis  délai maximal d'attente acceptable (en millisecondes)
     * @param vehiculesExclus liste des IDs de véhicules à exclure
     * @return un VehiculeRetour contenant le véhicule et sa date de retour, ou null
     *         si aucun trouvé
     * @throws SQLException en cas d'erreur d'accès à la base de données
     */
    public VehiculeRetour trouverVehiculeEnRetour(int nbPax, Timestamp departSouhaite,
            long delaiMaxMillis, List<Integer> vehiculesExclus)
            throws SQLException {

        // Calcul de la limite maximale d'attente (départ souhaité + délai max)
        Timestamp limiteDateRetour = new Timestamp(departSouhaite.getTime() + delaiMaxMillis);

        // Requête SQL pour trouver les véhicules en cours de trajet
        // qui reviendront dans le délai acceptable et avec une capacité suffisante
        StringBuilder sql = new StringBuilder(
                "SELECT v.id, v.reference, v.place, v.type_carburant, " +
                        "       MAX(p.date_heure_retour_aeroport) as date_retour_max " +
                        "FROM Vehicule v " +
                        "JOIN Planification p ON p.id_vehicule = v.id " +
                        "WHERE v.place >= ? " + // Capacité suffisante
                        "  AND p.date_heure_retour_aeroport > ? " + // Pas encore revenu (retour après départ souhaité)
                        "  AND p.date_heure_retour_aeroport <= ? "); // Revient dans le délai acceptable

        // Exclusion des véhicules déjà utilisés
        if (!vehiculesExclus.isEmpty()) {
            sql.append("  AND v.id NOT IN (");
            for (int i = 0; i < vehiculesExclus.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
            }
            sql.append(") ");
        }

        // Groupement par véhicule et tri par gaspillage (Best-Fit)
        sql.append("GROUP BY v.id, v.reference, v.place, v.type_carburant ");
        sql.append("ORDER BY (v.place - ?) ASC "); // Tri par gaspillage croissant (Best-Fit)
        sql.append("LIMIT 1"); // On ne prend que le meilleur

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            stmt.setInt(idx++, nbPax); // v.place >= ?
            stmt.setTimestamp(idx++, departSouhaite); // date_retour > ?
            stmt.setTimestamp(idx++, limiteDateRetour); // date_retour <= ?

            // Paramètres pour l'exclusion des véhicules
            for (Integer idExclu : vehiculesExclus) {
                stmt.setInt(idx++, idExclu);
            }

            stmt.setInt(idx++, nbPax); // Pour le ORDER BY (v.place - ?)

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Construction du véhicule trouvé
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id"));
                    vehicule.setReference(rs.getString("reference"));
                    vehicule.setPlace(rs.getInt("place"));
                    vehicule.setTypeCarburant(rs.getString("type_carburant"));

                    // Date de retour du véhicule (quand il sera disponible)
                    Timestamp dateRetour = rs.getTimestamp("date_retour_max");

                    return new VehiculeRetour(vehicule, dateRetour);
                }
            }
        }

        return null; // Aucun véhicule en retour trouvé
    }

    /**
     * Compare deux options d'assignation pour choisir la meilleure (Best-Fit +
     * retour véhicule).
     *
     * Cette méthode compare :
     * 1. Un véhicule disponible immédiatement
     * 2. Un véhicule en retour (qui sera disponible après son retour)
     *
     * Critères de comparaison dans l'ordre :
     * 1. Gaspillage (places vides) - le moins est le mieux
     * 2. Délai d'attente - si gaspillage égal, on préfère ne pas attendre
     *
     * @param vehiculeDisponible véhicule disponible immédiatement (peut être null)
     * @param vehiculeEnRetour   véhicule en retour (peut être null)
     * @param nbPassagers        nombre de passagers à transporter
     * @param departSouhaite     date/heure de départ souhaitée
     * @return true si le véhicule en retour est un meilleur choix, false sinon
     */
    public boolean vehiculeEnRetourEstMeilleur(Vehicule vehiculeDisponible,
            VehiculeRetour vehiculeEnRetour,
            int nbPassagers,
            Timestamp departSouhaite) {

        // Si pas de véhicule en retour, on prend le disponible
        if (vehiculeEnRetour == null || vehiculeEnRetour.getVehicule() == null) {
            return false;
        }

        // Si pas de véhicule disponible, on prend celui en retour
        if (vehiculeDisponible == null) {
            return true;
        }

        // Si l'option immédiate implique une scission mais que le véhicule en retour
        // peut prendre tous les passagers, on préfère attendre un peu.
        boolean disponiblePeutToutPrendre = vehiculeDisponible.getPlace() >= nbPassagers;
        boolean retourPeutToutPrendre = vehiculeEnRetour.getVehicule().getPlace() >= nbPassagers;
        if (!disponiblePeutToutPrendre && retourPeutToutPrendre) {
            return true;
        }
        if (disponiblePeutToutPrendre && !retourPeutToutPrendre) {
            return false;
        }

        // Calcul du gaspillage pour chaque option
        int gaspillageDispo = Math.max(0, vehiculeDisponible.getPlace() - nbPassagers);
        int gaspillageRetour = Math.max(0, vehiculeEnRetour.getVehicule().getPlace() - nbPassagers);

        // Critère principal : gaspillage minimal
        if (gaspillageRetour < gaspillageDispo) {
            // Le véhicule en retour gaspille moins de places → meilleur choix
            return true;
        } else if (gaspillageRetour > gaspillageDispo) {
            // Le véhicule disponible gaspille moins → on le garde
            return false;
        }

        // Gaspillage égal : on préfère le véhicule disponible immédiatement
        // (évite l'attente inutile)
        return false;
    }
}
