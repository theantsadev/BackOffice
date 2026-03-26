package com.hotel.service.planification;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotel.model.Planification;
import com.hotel.model.Reservation;
import com.hotel.service.DistanceService;
import com.hotel.service.ParametreService;

public class RouteCalculationService {

    private final DistanceService distanceService;
    private final ParametreService parametreService;

    public RouteCalculationService() {
        this.distanceService = new DistanceService();
        this.parametreService = new ParametreService();
    }

    public RouteCalculationService(DistanceService distanceService, ParametreService parametreService) {
        this.distanceService = distanceService;
        this.parametreService = parametreService;
    }

    public double getDureeTrajetMinutes(Reservation reservation) throws SQLException {
        double distanceKm = distanceService.getDistanceAeroportHotel(reservation.getId_hotel());
        if (distanceKm < 0) {
            throw new SQLException("Distance non trouvee pour l'hotel: " + reservation.getId_hotel());
        }

        double vitesseKmh = parametreService.getValeurByCle("vitesse_moyenne_kmh", 30.0);
        double dureeHeures = distanceKm / vitesseKmh;
        return dureeHeures * 60;
    }

    public Timestamp getDateHeureDepartAeroport(Reservation reservation) throws SQLException {
        double tempsAttenteMin = parametreService.getValeurByCle("temps_attente_min", 30.0);
        long attenteMillis = (long) (tempsAttenteMin * 60 * 1000);
        return new Timestamp(reservation.getDate_heure_arrivee().getTime() + attenteMillis);
    }

    public Timestamp getDateHeureRetourAeroport(Reservation reservation, double dureeMinutes) throws SQLException {
        Timestamp depart = getDateHeureDepartAeroport(reservation);
        long dureeMillis = (long) (dureeMinutes * 60 * 1000);
        return new Timestamp(depart.getTime() + 2 * dureeMillis);
    }

    public Timestamp calculerRetourPourGroupe(Timestamp depart, List<Reservation> groupe,
            PlanificationContext context) throws SQLException {
        List<Integer> hotelIds = new ArrayList<>();
        for (Reservation reservation : groupe) {
            if (!hotelIds.contains(reservation.getId_hotel())) {
                hotelIds.add(reservation.getId_hotel());
            }
        }

        List<Integer> hotelsTries = trierHotelsParDistanceAeroport(hotelIds, context);
        RouteMetrics metrics = calculerRouteDepuisHotels(hotelsTries, context);
        return new Timestamp(depart.getTime() + metrics.getTotalDurationMillis());
    }

    public RouteMetrics calculerRouteDepuisPlanifications(List<Planification> trip, PlanificationContext context)
            throws SQLException {
        List<Integer> hotelIds = new ArrayList<>();
        for (Planification planification : trip) {
            if (!hotelIds.contains(planification.getIdHotel())) {
                hotelIds.add(planification.getIdHotel());
            }
        }

        List<Integer> hotelsTries = trierHotelsParDistanceAeroport(hotelIds, context);
        return calculerRouteDepuisHotels(hotelsTries, context);
    }

    public Map<Integer, double[]> calculerMetriquesParHotel(List<Planification> trip, PlanificationContext context)
            throws SQLException {
        List<Integer> hotelIds = new ArrayList<>();
        for (Planification planification : trip) {
            if (!hotelIds.contains(planification.getIdHotel())) {
                hotelIds.add(planification.getIdHotel());
            }
        }

        List<Integer> hotelsTries = trierHotelsParDistanceAeroport(hotelIds, context);
        Map<Integer, double[]> metricsByHotel = new HashMap<>();

        int precedent = context.getAeroport().getId_hotel();
        double progressif = 0;
        for (int hotelId : hotelsTries) {
            double segmentKm = safeDistanceKm(precedent, hotelId, context);
            progressif += segmentKm;
            metricsByHotel.put(hotelId, new double[] { segmentKm, progressif });
            precedent = hotelId;
        }

        return metricsByHotel;
    }

    public List<Integer> trierHotelsParDistanceAeroport(List<Integer> hotelIds, PlanificationContext context) {
        List<Integer> sorted = new ArrayList<>(hotelIds);
        sorted.sort((a, b) -> {
            try {
                return Double.compare(
                        safeDistanceKm(context.getAeroport().getId_hotel(), a, context),
                        safeDistanceKm(context.getAeroport().getId_hotel(), b, context));
            } catch (SQLException e) {
                return 0;
            }
        });
        return sorted;
    }

    public RouteMetrics calculerRouteDepuisHotels(List<Integer> hotelIds, PlanificationContext context)
            throws SQLException {
        double distanceTotaleKm = 0;
        long dureeTotaleMillis = 0;
        int hotelActuel = context.getAeroport().getId_hotel();

        for (int hotelId : hotelIds) {
            double segmentKm = safeDistanceKm(hotelActuel, hotelId, context);
            distanceTotaleKm += segmentKm;
            dureeTotaleMillis += convertDistanceToMillis(segmentKm, context.getVitesseKmh());
            hotelActuel = hotelId;
        }

        double retourKm = safeDistanceKm(hotelActuel, context.getAeroport().getId_hotel(), context);
        distanceTotaleKm += retourKm;
        dureeTotaleMillis += convertDistanceToMillis(retourKm, context.getVitesseKmh());

        return new RouteMetrics(distanceTotaleKm, dureeTotaleMillis);
    }

    public double safeDistanceKm(int fromHotelId, int toHotelId, PlanificationContext context) throws SQLException {
        String key = fromHotelId < toHotelId
                ? fromHotelId + "_" + toHotelId
                : toHotelId + "_" + fromHotelId;

        Double cached = context.getDistanceCache().get(key);
        if (cached != null) {
            return cached;
        }

        double distanceKm = distanceService.getDistanceValeur(fromHotelId, toHotelId);
        double safeDistance = distanceKm < 0 ? 0 : distanceKm;
        context.getDistanceCache().put(key, safeDistance);
        return safeDistance;
    }

    public long convertDistanceToMillis(double distanceKm, double vitesseKmh) {
        double dureeHeures = distanceKm / vitesseKmh;
        return (long) (dureeHeures * 3600 * 1000);
    }
}
