package com.example.livemart.model;

import com.example.livemart.model.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "sold_by_id")
    @JsonIgnoreProperties({"itemsCurrentlyBeingSold", "password", "authorizedBy", "otpObject", "cart", "orders", "purchaseHistory"})
    User soldBy;

    @ManyToOne
    @JoinColumn(name = "bought_by_id")
    @JsonIgnoreProperties({"itemsCurrentlyBeingSold", "password", "authorizedBy", "otpObject", "cart", "orders", "purchaseHistory"})
    User boughtBy;

    @Enumerated(EnumType.STRING)
    OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name = "ordered_item_id")
    private Item orderedItem;

    private Integer orderQuantity;
    private String units;
    private String deliveryAddress;
    private String deliveryMode; // offline or online

    private LocalDate expectedDeliveryDate;

    @ManyToOne
    @JoinColumn(name = "purchase_involving_order_id")
    private Purchase purchaseInvolvingOrder;

    // Time related
    private LocalDateTime orderCreatedAt;
    private LocalDateTime orderLastUpdatedAt;

    @PrePersist
    public void onCreate(){
        this.orderCreatedAt = LocalDateTime.now();
        this.orderLastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        this.orderLastUpdatedAt = LocalDateTime.now();
    }
}
