package com.example.livemart.service;

import com.example.livemart.model.ItemReview;
import com.example.livemart.repository.ItemRepository;
import com.example.livemart.repository.ItemReviewRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemReviewService {
    private ItemReviewRepository itemReviewRepository;

    public ItemReviewService(ItemReviewRepository itemReviewRepository){
        this.itemReviewRepository = itemReviewRepository;
    }

    public void addReview(ItemReview itemReview){
        itemReviewRepository.save(itemReview);
    }
}
