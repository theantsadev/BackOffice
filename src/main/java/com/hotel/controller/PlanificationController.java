package com.hotel.controller;

import com.hotel.service.PlanificationService;
import com.hotel.service.TokenService;

import servlet.annotations.Controller;
import servlet.annotations.Json;
import servlet.annotations.mapping.GetMapping;
import servlet.annotations.RequestParam;
import servlet.api.ApiResponse;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Controller
public class PlanificationController {

    private PlanificationService planificationService = new PlanificationService();
    private TokenService tokenService = new TokenService();

    /**
     * GET /planifications?date=YYYY-MM-DD
     * Planification automatique: récupère les réservations de la date,
     * assigne automatiquement un véhicule à chaque réservation non assignée,
     * et retourne les planifications + réservations non assignées
     */
    @Json
    @GetMapping(value = "/planifications")
    public ApiResponse<?> getPlanifications(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "date") String dateStr,
            @RequestParam(name = "depart_groupe") String departGroupeStr) throws SQLException {

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
            Timestamp departGroupe = null;
            if (departGroupeStr != null && !departGroupeStr.trim().isEmpty() && !"all".equalsIgnoreCase(departGroupeStr)) {
                try {
                    long departMillis = Long.parseLong(departGroupeStr.trim());
                    departGroupe = new Timestamp(departMillis);
                } catch (NumberFormatException e) {
                    return ApiResponse.error(400,
                            "Le paramètre 'depart_groupe' doit être un timestamp en millisecondes", null);
                }
            }

            Map<String, Object> result = planificationService.planifierAutoParDate(date, departGroupe);
            return ApiResponse.success(result);
        } catch (Exception e) {
            String errorMessage = "Erreur lors de la planification automatique: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    
}
