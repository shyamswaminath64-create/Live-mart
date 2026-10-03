package com.example.livemart.model;

import com.example.livemart.model.enums.AuthorizedBy;
import com.example.livemart.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name="users")
public class User {
    // Properties of the User object itself
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 50)
    private String storeName; // null for Customers

    @Column(length = 100)
    private String address; // Store address for retailers and wholesalers

    @Column(unique = true, nullable = false)
    private String email;

    private String password = "";

    @Column(length = 10)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AuthorizedBy authorizedBy;

    private Boolean authorized = false;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "otp_object_id")
    private UserOTP otpObject;

    // Related to items sold
    @OneToMany(mappedBy = "soldBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> itemsCurrentlyBeingSold = new ArrayList<>(); // null for Customer

//    @ElementCollection
//    @CollectionTable(name = "user_cart", joinColumns = @JoinColumn(name = "user_id"))
//    @MapKeyJoinColumn(name = "items_id")
//    private Map<Item, Integer> cart = new HashMap<>(); // null for Wholesaler

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cart = new ArrayList<>();

    @OneToMany(mappedBy = "soldBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>(); // empty for Customer

    @OneToMany(mappedBy = "initiatedBy", cascade = CascadeType.ALL)
    private List<Purchase> purchaseHistory = new ArrayList<>(); //empty for Wholesaler

    private Double latitude;
    private Double longitude;
}
