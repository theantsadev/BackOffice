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
}
