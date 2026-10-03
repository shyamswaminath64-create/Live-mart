package com.example.livemart.service;

import com.example.livemart.model.Purchase;
import com.example.livemart.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;

    public PurchaseService(PurchaseRepository purchaseRepository){
        this.purchaseRepository = purchaseRepository;
    }

    public void save(Purchase purchase) {
        purchaseRepository.save(purchase);
    }
}
