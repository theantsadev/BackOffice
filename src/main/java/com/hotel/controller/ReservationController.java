package com.hotel.controller;

import com.hotel.model.Reservation;
import com.hotel.service.ReservationService;

import servlet.annotations.Controller;
import servlet.annotations.Json;
import servlet.annotations.mapping.GetMapping;
import servlet.annotations.mapping.PostMapping;
import servlet.annotations.RequestParam;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class ReservationController {

    private ReservationService reservationService = new ReservationService();

    @Json
    @GetMapping(value = "/reservations")
    public List<Reservation> getReservations(@RequestParam(name = "date") String dateStr) throws Exception {
        if (dateStr != null && !dateStr.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);
            return reservationService.getReservationByDate(date);
        } else {
            return reservationService.getAllReservation();
        }
    }

    @Json
    @PostMapping(value = "/reservations")
    public Reservation reserver(
            @RequestParam(name = "id_client") String id_client,
            @RequestParam(name = "nb_passager") int nb_passager,
            @RequestParam(name = "date_heure_arrivee") String date_heure_arrivee,
            @RequestParam(name = "id_hotel") int id_hotel) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date date = sdf.parse(date_heure_arrivee);
        Timestamp timestamp = new Timestamp(date.getTime());

        return reservationService.reserver(id_client, nb_passager, timestamp, id_hotel);
    }
}
