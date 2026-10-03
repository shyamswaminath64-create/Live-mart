package com.example.livemart.dto;

import com.example.livemart.model.Item;
import com.example.livemart.model.User;
import com.example.livemart.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class UserHomeDTO {
    private String name;
    private String storeName;
    private String address;
    private String email;
    private String phoneNumber;
    private Role role;
    private List<ItemDTO> itemsCurrentlyBeingSold;

    private static List<ItemDTO> convertAlltoDTO(List<Item> items){
        List<ItemDTO> itemDTOList = new ArrayList<>();

        for (Item item : items){
            itemDTOList.add(ItemDTO.mapItemToDTO(item));
        }
        return itemDTOList;
    }

    public static UserHomeDTO mapFromUser(User user){
        if (user.getRole() == Role.Customer)
            return new UserHomeDTO(user.getName(), null, null, null, null, user.getRole(), null);

        return new UserHomeDTO(user.getName(), user.getStoreName(), user.getAddress(), user.getEmail(), user.getPhoneNumber(), user.getRole(), convertAlltoDTO(user.getItemsCurrentlyBeingSold()));
    }
}
