package com.hotel.controller;

import com.hotel.model.Hotel;
import com.hotel.service.HotelService;
import com.hotel.util.JsonUtil;
import servlet.annotations.Controller;
import servlet.api.ApiResponse;
import servlet.annotations.mapping.GetMapping;
import servlet.ModelView;

import java.util.List;

@Controller
public class HotelController {
    
    private HotelService hotelService = new HotelService();
    
    @GetMapping(value = "/hotels")
    public ModelView getAllHotels() {
        ModelView mv = new ModelView();
        try {
            List<Hotel> hotels = hotelService.getAllHotel();
            ApiResponse<List<Hotel>> response = ApiResponse.success(hotels);
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
