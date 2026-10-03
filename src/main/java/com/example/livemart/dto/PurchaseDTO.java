package com.example.livemart.dto;

import com.example.livemart.model.Order;
import com.example.livemart.model.Purchase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDTO {
    private Long id;

    private Long userInitiatedByID;
    private String userInitiatedByName;

    private List<OrderDTO> ordersInPurchase;
    private Double totalPrice;

    private LocalDateTime purchaseInitiationTime;

    public static PurchaseDTO createDTOFromPurchase(Purchase purchase){
        PurchaseDTO purchaseDTO = new PurchaseDTO();
        purchaseDTO.setId(purchase.getId());
        purchaseDTO.setTotalPrice(purchase.getTotalPrice());
        purchaseDTO.setPurchaseInitiationTime(purchase.getPurchaseInitiationTime());
        purchaseDTO.setUserInitiatedByID(purchase.getInitiatedBy().getId());
        purchaseDTO.setUserInitiatedByName(purchase.getInitiatedBy().getName());

        List<OrderDTO> orderDTOS = new ArrayList<>();
        for (Order order : purchase.getOrdersInPurchase()){
            orderDTOS.add(OrderDTO.createDTOFromOrder(order));
        }
        purchaseDTO.setOrdersInPurchase(orderDTOS);

        return purchaseDTO;
    }
}
