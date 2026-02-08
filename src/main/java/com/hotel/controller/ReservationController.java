package com.hotel.controller;

import com.hotel.model.Reservation;
import com.hotel.service.ReservationService;
import com.hotel.util.JsonUtil;
import servlet.annotations.Controller;
import servlet.api.ApiResponse;
import servlet.annotations.mapping.GetMapping;
import servlet.annotations.mapping.PostMapping;
import servlet.annotations.RequestParam;
import servlet.ModelView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class ReservationController {
    
    private ReservationService reservationService = new ReservationService();
    
    @GetMapping(value = "/reservations")
    public ModelView getReservations(@RequestParam(name = "date") String dateStr) {
        ModelView mv = new ModelView();
        try {
            if (dateStr != null && !dateStr.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(dateStr);
                List<Reservation> reservations = reservationService.getReservationByDate(date);
                ApiResponse<List<Reservation>> response = ApiResponse.success(reservations);
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            } else {
                List<Reservation> reservations = reservationService.getAllReservation();
                ApiResponse<List<Reservation>> response = ApiResponse.success(reservations);
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            }
            mv.setView("json-response.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<String> response = ApiResponse.error(500, e.getMessage(), null);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        }
        return mv;
    }
    
    @PostMapping(value = "/reservations")
    public ModelView reserver(
            @RequestParam(name = "id_client") String id_client,
            @RequestParam(name = "nb_passager") int nb_passager,
            @RequestParam(name = "date_heure_arrivee") String date_heure_arrivee,
            @RequestParam(name = "id_hotel") int id_hotel) {
        
        ModelView mv = new ModelView();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date date = sdf.parse(date_heure_arrivee);
            Timestamp timestamp = new Timestamp(date.getTime());
            
            Reservation reservation = reservationService.reserver(id_client, nb_passager, timestamp, id_hotel);
            ApiResponse<Reservation> response = ApiResponse.success(reservation);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<String> response = ApiResponse.error(500, e.getMessage(), null);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        }
        return mv;
    }
}
