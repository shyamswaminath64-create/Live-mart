package com.example.livemart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutInfo {
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;
}
