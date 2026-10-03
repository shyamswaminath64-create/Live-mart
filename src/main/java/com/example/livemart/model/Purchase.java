package com.example.livemart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name="purchase")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "initiated_by_id")
    @JsonIgnoreProperties({"itemsCurrentlyBeingSold", "password", "authorizedBy", "otpObject", "cart", "orders", "purchaseHistory"})
    private User initiatedBy; // Contains shipping address so no need for a column here

    @OneToMany(mappedBy = "purchaseInvolvingOrder", cascade = CascadeType.ALL)
    private List<Order> ordersInPurchase = new ArrayList<>();
    private Double totalPrice = 0.0;

    private LocalDateTime purchaseInitiationTime;

    @PrePersist
    public void onCreate(){
        this.purchaseInitiationTime = LocalDateTime.now();
    }
}
