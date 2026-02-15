package com.hotel.controller;

import com.hotel.model.Hotel;
import com.hotel.service.HotelService;

import servlet.annotations.Controller;
import servlet.annotations.Json;
import servlet.annotations.mapping.GetMapping;

import java.sql.SQLException;
import java.util.List;

@Controller
public class HotelController {

    private HotelService hotelService = new HotelService();

    @Json
    @GetMapping(value = "/hotels")
    public List<Hotel> getAllHotels() throws SQLException {
         return hotelService.getAllHotel();
    }
}
