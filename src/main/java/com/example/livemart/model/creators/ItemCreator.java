package com.example.livemart.model.creators;

import com.example.livemart.model.Item;
import com.example.livemart.model.enums.AvailabilityStatus;
import lombok.Data;
import org.apache.commons.lang3.time.DateParser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

@Data
public class ItemCreator {
    private String category;
    private String itemName;
    private String itemDescription;
    private Double price;
    private String itemImageUrl = "";

    private Integer quantityAvailable;
    private String qtyUnits;

    private String deliveryMode;

    private String availabilityDate;

    private String availabilityStatus;

    public static Item mapToItem(ItemCreator itemCreator){
        Item item = new Item();
        mapToExistingItem(item, itemCreator);

        item.setRating(0);

        return item;
    }

    public static Item mapToExistingItem(Item item, ItemCreator itemCreator){
        item.setCategory(itemCreator.getCategory());
        item.setItemName(itemCreator.getItemName());
        item.setItemDescription(itemCreator.getItemDescription());
        item.setPrice(itemCreator.getPrice());
        item.setItemImageUrl(itemCreator.getItemImageUrl());
        item.setQuantityAvailable(itemCreator.getQuantityAvailable());
        item.setQtyUnits(itemCreator.getQtyUnits());
        item.setDeliveryMode(itemCreator.getDeliveryMode());

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH);
        LocalDate dateOb = LocalDate.parse(itemCreator.getAvailabilityDate(), dateTimeFormatter);
        item.setAvailabilityDate(dateOb);

        item.setAvailabilityStatus(AvailabilityStatus.valueOf(itemCreator.getAvailabilityStatus()));

        return item;
    }
}
