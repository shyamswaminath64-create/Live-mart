package com.example.livemart.dto;

import com.example.livemart.model.Item;
import com.example.livemart.model.ItemReview;
import com.example.livemart.model.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class ItemDTO {
    private Long id;

    private String soldBy; // Store name only, not username

    private String category;
    private String itemName;
    private String itemDescription;
    private Double price;
    private String itemImageUrl;
    private Integer quantityAvailable;

    private String qtyUnits;
    private LocalDate availabilityDate;
    private String deliveryMode;

    private AvailabilityStatus availabilityStatus;
    private Integer rating;

    private List<ItemReview> reviewsForItem;

    public static ItemDTO mapItemToDTO(Item item){
        return new ItemDTO(item.getId(), item.getSoldBy().getStoreName(), item.getCategory(), item.getItemName(), item.getItemDescription()
        , item.getPrice(), item.getItemImageUrl(), item.getQuantityAvailable(), item.getQtyUnits(), item.getAvailabilityDate(), item.getDeliveryMode(),
                item.getAvailabilityStatus(), item.getRating(), item.getReviewsForItem());
    }
}
