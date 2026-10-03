package com.example.livemart.repository;

import com.example.livemart.model.CartItem;
import com.example.livemart.model.Item;
import com.example.livemart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    CartItem findCartItemByItemAndUser(Item item, User user);
}
