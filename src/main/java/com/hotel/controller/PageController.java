package com.hotel.controller;

import com.hotel.model.Hotel;
import com.hotel.service.HotelService;
import servlet.annotations.Controller;
import servlet.ModelView;
import servlet.annotations.mapping.GetMapping;

import java.util.List;

@Controller
public class PageController {
    
    @GetMapping(value = "/pages/")
    public ModelView index() {
        ModelView mv = new ModelView();
        mv.setView("pages/index.jsp");
        return mv;
    }
    
    @GetMapping(value = "/pages/formulaire-reservation")
    public ModelView formulaireReservation() {
        ModelView mv = new ModelView();
        try {
            HotelService hotelService = new HotelService();
            List<Hotel> hotels = hotelService.getAllHotel();
            mv.addAttribute("hotels", hotels);
            mv.setView("pages/formulaire-reservation.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            mv.addAttribute("error", e.getMessage());
            mv.setView("pages/error.jsp");
        }
        return mv;
    }
    
    @GetMapping(value = "/pages/liste-reservations")
    public ModelView listeReservations() {
        ModelView mv = new ModelView();
        mv.setView("pages/liste-reservations.jsp");
        return mv;
    }
}
