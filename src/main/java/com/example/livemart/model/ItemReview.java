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
@Table(name = "itemReview")
public class ItemReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @JsonIgnoreProperties({"soldBy", "category", "itemName", "itemDescription",
            "price", "itemImageUrl", "quantityAvailable", "qtyUnits", "deliveryMode",
            "availabilityDate", "availabilityStatus", "rating", "ordersForItem", "cartsContainingItem", "reviewsForItem"})
    private Item item;

    @Column(length = 200)
    private String review;
    private Integer ratingGiven;
}
