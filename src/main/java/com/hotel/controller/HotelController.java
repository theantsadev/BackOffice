package com.hotel.controller;

import com.hotel.model.Hotel;
import com.hotel.service.HotelService;

import servlet.annotations.Controller;
import servlet.annotations.Json;
import servlet.annotations.mapping.GetMapping;
import servlet.api.ApiResponse;

import java.sql.SQLException;
import java.util.List;

@Controller
public class HotelController {

    private HotelService hotelService = new HotelService();

    @Json
    @GetMapping(value = "/hotels")
    public ApiResponse<?> getAllHotels() throws SQLException {
        try {
            List<Hotel> hotels = hotelService.getAllHotel();
            return ApiResponse.success(hotels);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage(), null);
        }
    }
}
