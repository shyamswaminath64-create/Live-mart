package com.example.livemart.dto;

import com.example.livemart.model.Order;
import com.example.livemart.model.User;
import com.example.livemart.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;

    private Long soldById;
    private String soldByName;

    private Long boughtById;
    private String boughtByName;

    private Long itemId;
    private String itemName;

    private Integer orderQuantity;
    private String units;

    private OrderStatus orderStatus;

    public static OrderDTO createDTOFromOrder(Order order){
        return new OrderDTO(order.getOrderId(), order.getSoldBy().getId(), order.getSoldBy().getStoreName(), order.getBoughtBy().getId(), order.getBoughtBy().getName(), order.getOrderedItem().getId(),
                order.getOrderedItem().getItemName(), order.getOrderQuantity(), order.getUnits(), order.getOrderStatus());
    }
}
