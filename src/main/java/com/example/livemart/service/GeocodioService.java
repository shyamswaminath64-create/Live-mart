package com.example.livemart.service;

import com.example.livemart.dto.GeocodioResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
public class GeocodioService {

    @Value("${geocodio.api.key}")
    private String apiKey;

    private static final String GEOCODIO_URL = "https://api.geocod.io/v1.7/geocode";

    public GeocodioResponse.Result geocode(String address) {
        System.out.println(address);
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }

        RestTemplate restTemplate = new RestTemplate();

        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = GEOCODIO_URL + "?q=" + encodedAddress + "&api_key=" + apiKey;

        System.out.println("Geocod.io URL: " + url);

        try {
            GeocodioResponse response = restTemplate.getForObject(url, GeocodioResponse.class);
            System.out.println(response);
            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                return (response.getResults().get(0));
            } else {
                return (null);
            }
        }
        catch (HttpClientErrorException e) {
            System.err.println("HTTP Status: " + e.getStatusCode());
            System.err.println("Full Response: " + e.getResponseBodyAsString());
            return null;
        }
    }
}
