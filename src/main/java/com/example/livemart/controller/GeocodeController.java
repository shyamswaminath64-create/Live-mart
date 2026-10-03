package com.example.livemart.controller;

import com.example.livemart.dto.GeocodioResponse;
import com.example.livemart.service.GeocodioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geocode") // need to set endpoint properly
public class GeocodeController {

    @Autowired
    private GeocodioService geocodioService;

    @GetMapping //set endpoint properly
    public GeocodioResponse.Result getCoordinates(@RequestParam String address) {
        return geocodioService.geocode(address);
    }
}