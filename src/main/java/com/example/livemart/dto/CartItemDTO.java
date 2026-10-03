package com.example.livemart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemDTO {
    private ItemDTO itemDTO;
    private Integer qty;
}
