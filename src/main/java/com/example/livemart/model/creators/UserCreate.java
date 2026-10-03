package com.example.livemart.model.creators;

import com.example.livemart.model.enums.AuthorizedBy;
import com.example.livemart.model.enums.Role;
import com.example.livemart.model.User;
import lombok.Data;

@Data
public class UserCreate {
    private String name = "";
    private String address = "";
    private String email = "";
    private String phoneNumber = "";
    private String password;
    private String storeName = "";

    private String role = "Customer";
    private AuthorizedBy authorizedBy;

    public static User mapToUser(UserCreate userCreate){
        User user = new User();
        user.setName(userCreate.getName());
        user.setRole(Role.valueOf(userCreate.getRole()));
        user.setAddress(userCreate.getAddress());
        user.setAuthorizedBy(userCreate.getAuthorizedBy());
        user.setPassword(userCreate.getPassword());
        user.setEmail(userCreate.getEmail());
        user.setPhoneNumber(userCreate.getPhoneNumber());
        user.setStoreName(userCreate.getStoreName());

        return user;
    }
}
