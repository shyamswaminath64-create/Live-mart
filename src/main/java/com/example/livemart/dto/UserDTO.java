package com.example.livemart.dto;

import com.example.livemart.model.enums.Role;
import com.example.livemart.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String name;
    private String address;
    private String email;
    private String phoneNumber;
    private Role role;

    public static UserDTO mapUserToDTO(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setAddress(user.getAddress());
        userDTO.setRole(user.getRole());
        userDTO.setName(user.getName());
        userDTO.setPhoneNumber(user.getPhoneNumber());

        return userDTO;
    }
}
