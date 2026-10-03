package com.example.livemart.service;

import com.example.livemart.model.Item;
import com.example.livemart.model.User;
import com.example.livemart.model.creators.ItemCreator;
import com.example.livemart.model.enums.AvailabilityStatus;
import com.example.livemart.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElse(null);
    }

    public Item getItemByIdOwnedByUser(Long itemId, User user){
        return itemRepository.findByIdAndSoldBy(itemId, user).orElse(null);
    }

    public boolean save(Item item) {
        itemRepository.save(item);
        return true;
    }

    public boolean delete(Long itemId) {
        Item item = getItemById(itemId);
        itemRepository.delete(item);
        return true;
    }

    public List<Item> findByCategory(String categoryName) {
        return itemRepository.findByCategory(categoryName);
    }

    public List<Item> getAllItemsSortedByCategoryAsc() {
        return itemRepository.findAllByOrderByCategoryAsc();
    }

    public List<Item> getAllItemsSortedByCategoryDesc() {
        return itemRepository.findAllByOrderByCategoryDesc();
    }

    public Double getPriceOfItem(Long itemId) {
        return getItemById(itemId).getPrice();
    }

    public boolean setPriceOfItem(Long itemId, Double price) {
        Item currentItem = getItemById(itemId);
        currentItem.setPrice(price);
        itemRepository.save(currentItem);
        return true;
    }

    public Integer getQuantityAvailable(Long itemId) {
        return getItemById(itemId).getQuantityAvailable();
    }

    public boolean setQuantityAvailable(Long itemId, Integer quantity) {
        Item currentItem = getItemById(itemId);
        currentItem.setQuantityAvailable(quantity);
        itemRepository.save(currentItem);
        return true;
    }

    public AvailabilityStatus getAvailabilityStatus(Long itemId) {
        return getItemById(itemId).getAvailabilityStatus();
    }

    public boolean setAvailabilityStatus(Long itemId, AvailabilityStatus availabilityStatus) {
        Item currentItem = getItemById(itemId);
        currentItem.setAvailabilityStatus(availabilityStatus);
        itemRepository.save(currentItem);
        return true;
    }

    public String getImageUrl(Long itemId) {
        return getItemById(itemId).getItemImageUrl();
    }

    public boolean setImageUrl(Long itemId, String imageUrl) {
        Item currentItem = getItemById(itemId);
        currentItem.setItemImageUrl(imageUrl);
        itemRepository.save(currentItem);
        return true;
    }

    public String getItemDescription(Long itemId) {
        return getItemById(itemId).getItemDescription();
    }

    public boolean setItemDescription(Long itemId, String itemDescription) {
        Item currentItem = getItemById(itemId);
        currentItem.setItemDescription(itemDescription);
        itemRepository.save(currentItem);
        return true;
    }

    public Integer getRating(Long itemId) {
        return getItemById(itemId).getRating();
    }

    public boolean setRating(Long itemId, Integer rating) {
        Item currentItem = getItemById(itemId);
        currentItem.setRating(rating);
        itemRepository.save(currentItem);
        return true;
    }

    public static boolean validateItemCreator(ItemCreator itemCreator){
        if (itemCreator.getCategory().length() > 50 || itemCreator.getItemName().length() > 50 || itemCreator.getItemDescription().length() > 50 ||
                itemCreator.getQuantityAvailable() < 0 || itemCreator.getQtyUnits().length() > 5 || itemCreator.getDeliveryMode().length() > 10) {
            return false;
        }

        try{
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH);
            LocalDate.parse(itemCreator.getAvailabilityDate(), dateTimeFormatter);
        }
        catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    public List<Item> searchSimilar(String query){
        return itemRepository.searchSimilar(query);
    }
}
