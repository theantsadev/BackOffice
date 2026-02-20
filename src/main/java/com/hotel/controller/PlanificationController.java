package com.hotel.controller;

import com.hotel.model.Planification;
import com.hotel.model.Reservation;
import com.hotel.model.Vehicule;
import com.hotel.service.PlanificationService;
import com.hotel.service.TokenService;

import servlet.annotations.Controller;
import servlet.annotations.Json;
import servlet.annotations.mapping.GetMapping;
import servlet.annotations.mapping.PostMapping;
import servlet.annotations.RequestParam;
import servlet.api.ApiResponse;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class PlanificationController {

    private PlanificationService planificationService = new PlanificationService();
    private TokenService tokenService = new TokenService();

    /**
     * GET /planifications?date=YYYY-MM-DD
     * Récupère les planifications d'une date donnée
     */
    @Json
    @GetMapping(value = "/planifications")
    public ApiResponse<?> getPlanifications(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "date") String dateStr) throws SQLException {
        
        // Vérifier le token
        if (token == null || token.isEmpty()) {
            return ApiResponse.error(403, "Token manquant", null);
        }
        
        if (!tokenService.isTokenValid(token)) {
            return ApiResponse.error(403, "Token invalide ou expiré", null);
        }
        
        try {
            if (dateStr == null || dateStr.isEmpty()) {
                return ApiResponse.error(400, "Le paramètre 'date' est requis (format: YYYY-MM-DD)", null);
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);
            List<Planification> planifications = planificationService.getPlanificationsByDate(date);
            return ApiResponse.success(planifications);
        } catch (Exception e) {
            String errorMessage = "Erreur lors de la récupération des planifications: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    /**
     * POST /planifications
     * Crée une nouvelle planification
     */
    @Json
    @PostMapping(value = "/planifications")
    public ApiResponse<?> createPlanification(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "id_reservation") int idReservation,
            @RequestParam(name = "id_vehicule", required = false, defaultValue = "-1") int idVehicule,
            @RequestParam(name = "date_heure_depart", required = false) String dateHeureDepartStr,
            @RequestParam(name = "date_heure_retour", required = false) String dateHeureRetourStr) throws SQLException {
        
        // Vérifier le token
        if (token == null || token.isEmpty()) {
            return ApiResponse.error(403, "Token manquant", null);
        }
        
        if (!tokenService.isTokenValid(token)) {
            return ApiResponse.error(403, "Token invalide ou expiré", null);
        }
        
        try {
            Timestamp dateHeureDepart;
            Timestamp dateHeureRetour;
            int vehiculeId = idVehicule;

            // Si les dates ne sont pas fournies, les calculer automatiquement
            if (dateHeureDepartStr == null || dateHeureDepartStr.isEmpty()) {
                dateHeureDepart = planificationService.getDateHeureDepartAeroport(idReservation);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                dateHeureDepart = new Timestamp(sdf.parse(dateHeureDepartStr).getTime());
            }

            if (dateHeureRetourStr == null || dateHeureRetourStr.isEmpty()) {
                dateHeureRetour = planificationService.getDateHeureRetourAeroport(idReservation);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                dateHeureRetour = new Timestamp(sdf.parse(dateHeureRetourStr).getTime());
            }

            // Si le véhicule n'est pas fourni, le sélectionner automatiquement
            if (vehiculeId < 0) {
                Vehicule vehicule = planificationService.getVehiculeApproprieForReservation(idReservation);
                if (vehicule == null) {
                    return ApiResponse.error(400, "Aucun véhicule disponible pour cette réservation", null);
                }
                vehiculeId = vehicule.getId();
            } else {
                // Vérifier la disponibilité du véhicule
                if (!planificationService.estVoitureDisponible(vehiculeId, dateHeureDepart, dateHeureRetour)) {
                    return ApiResponse.error(400, "Le véhicule n'est pas disponible sur ce créneau", null);
                }
            }

            Planification planification = planificationService.planifier(
                idReservation, vehiculeId, dateHeureDepart, dateHeureRetour);
            
            if (planification != null) {
                return ApiResponse.success(planification);
            } else {
                return ApiResponse.error(500, "Erreur lors de la création de la planification", null);
            }
        } catch (Exception e) {
            String errorMessage = "Erreur lors de la création de la planification: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    /**
     * GET /reservations/non-assignees
     * Récupère les réservations sans planification
     */
    @Json
    @GetMapping(value = "/reservations/non-assignees")
    public ApiResponse<?> getReservationsNonAssignees(
            @RequestParam(name = "token") String token) throws SQLException {
        
        // Vérifier le token
        if (token == null || token.isEmpty()) {
            return ApiResponse.error(403, "Token manquant", null);
        }
        
        if (!tokenService.isTokenValid(token)) {
            return ApiResponse.error(403, "Token invalide ou expiré", null);
        }
        
        try {
            List<Reservation> reservations = planificationService.getReservationsNonAssignees();
            return ApiResponse.success(reservations);
        } catch (Exception e) {
            String errorMessage = "Erreur lors de la récupération des réservations: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }
}
