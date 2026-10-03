package com.example.livemart.model.creators;

import lombok.Data;

@Data
public class ProfileCreator {
    private String role = "Customer";
    private String address = "";
    private String phoneNumber = "";
    private String storeName = "";
}
