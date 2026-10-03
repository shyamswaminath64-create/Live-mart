package com.example.livemart.model.creators;

import com.example.livemart.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdator {
    private OrderStatus orderStatus;
    private String expectedDeliveryDate;
}
