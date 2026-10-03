package com.example.livemart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cart_item", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_id"}))
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"itemsCurrentlyBeingSold", "password", "authorizedBy", "otpObject", "cart", "orders", "purchaseHistory"})
    private User user; // This item exists in this users cart

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
    private int qtyInCart;

    public CartItem(User user, Item item, int qtyNeeded) {
        this.user = user;
        this.item = item;
        this.qtyInCart = qtyNeeded;
    }
}
