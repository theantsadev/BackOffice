package com.hotel.controller;

import com.hotel.model.Reservation;
import com.hotel.service.ReservationService;
import com.hotel.service.TokenService;

import servlet.annotations.Controller;
import servlet.annotations.Json;
import servlet.annotations.mapping.GetMapping;
import servlet.annotations.mapping.PostMapping;
import servlet.annotations.RequestParam;
import servlet.api.ApiResponse;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class ReservationController {

    private ReservationService reservationService = new ReservationService();
    private TokenService tokenService = new TokenService();

    @Json
    @GetMapping(value = "/reservations")
    public ApiResponse<?> getReservations(
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
            if (dateStr != null && !dateStr.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(dateStr);
                List<Reservation> reservations = reservationService.getReservationByDate(date);
                return ApiResponse.success(reservations);
            } else {
                List<Reservation> reservations = reservationService.getAllReservation();
                return ApiResponse.success(reservations);
            }
        } catch (Exception e) {
            String errorMessage = "Error fetching reservations: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    @Json
    @PostMapping(value = "/reservations")
    public ApiResponse<?> reserver(
            @RequestParam(name = "id_client") String id_client,
            @RequestParam(name = "nb_passager") int nb_passager,
            @RequestParam(name = "date_heure_arrivee") String date_heure_arrivee,
            @RequestParam(name = "id_hotel") int id_hotel) throws SQLException {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date date = sdf.parse(date_heure_arrivee);
            Timestamp timestamp = new Timestamp(date.getTime());

            Reservation reservation = reservationService.reserver(id_client, nb_passager, timestamp, id_hotel);
            return ApiResponse.success(reservation);
        } catch (Exception e) {
            String errorMessage = "Error creating reservation: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }
}
