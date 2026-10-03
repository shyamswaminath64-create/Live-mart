package com.example.livemart.model;

import com.example.livemart.model.enums.AvailabilityStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sold_by_id")
    @JsonIgnoreProperties({"itemsCurrentlyBeingSold", "password", "authorizedBy", "otpObject", "cart", "orders", "purchaseHistory"})
    private User soldBy;

    @Column(length=50)
    private String category;

    @Column(length=50, nullable = false)
    private String itemName;

    @Column(length = 100)
    private String itemDescription;

    private Double price;

    private String itemImageUrl = ""; //

    @Min(0)
    private Integer quantityAvailable; // Show this to customers

    @Column(length = 5)
    private String qtyUnits; //stock details

    @Column(length = 10)
    private String deliveryMode = "OFFLINE";

    private LocalDate availabilityDate = null;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;

    @Min(value = 0)
    @Max(value = 5)
    private Integer rating;

    @OneToMany(mappedBy = "orderedItem", cascade = CascadeType.ALL)
    private List<Order> ordersForItem = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> cartsContainingItem = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemReview> reviewsForItem = new ArrayList<>();
}
