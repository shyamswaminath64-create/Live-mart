package com.example.livemart.service;

import com.example.livemart.model.CartItem;
import com.example.livemart.model.Item;
import com.example.livemart.model.User;
import com.example.livemart.model.enums.AvailabilityStatus;
import com.example.livemart.repository.CartItemRepository;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;

    public CartService(CartItemRepository cartItemRepository){
        this.cartItemRepository = cartItemRepository;
    }

    public CartItem findByItemIdAndUser(Item item, User user){
        return cartItemRepository.findCartItemByItemAndUser(item, user);
    }

    public Pair<Boolean, String> verifyCart(List<CartItem> cartItemList){
        for (CartItem cartItem : cartItemList){
            if (cartItem.getItem().getAvailabilityStatus() == AvailabilityStatus.UNAVAILABLE){
                return new Pair<>(false, "Some items in cart are unavailable");
            }
            if (cartItem.getQtyInCart() > cartItem.getItem().getQuantityAvailable()){
                return new Pair<>(false, "Some items exceed quantity available");
            }
        }
        return new Pair<>(true, "");
    }
}
