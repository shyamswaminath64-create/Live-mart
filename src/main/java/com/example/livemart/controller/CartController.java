package com.example.livemart.controller;

import com.example.livemart.dto.CartItemDTO;
import com.example.livemart.dto.CheckoutInfo;
import com.example.livemart.dto.ItemDTO;
import com.example.livemart.model.*;
import com.example.livemart.model.enums.AvailabilityStatus;
import com.example.livemart.model.enums.OrderStatus;
import com.example.livemart.model.enums.Role;
import com.example.livemart.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/cart")
public class CartController {
    private final ItemService itemService;
    private final UserService userService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PurchaseService purchaseService;

    public CartController(ItemService itemService, UserService userService, CartService cartService, OrderService orderService, PurchaseService purchaseService){
        this.itemService = itemService;
        this.userService = userService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.purchaseService = purchaseService;
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/")
    public ResponseEntity<List<CartItemDTO>> viewCart(Authentication authentication){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Wholesaler);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        List<CartItem> cartItems = user.getCart();

        List<CartItemDTO> cartDTOList = new ArrayList<>();
        for (CartItem cartItem : cartItems){
            cartDTOList.add(new CartItemDTO(ItemDTO.mapItemToDTO(cartItem.getItem()), cartItem.getQtyInCart()));
        }

        return new ResponseEntity<>(cartDTOList, HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @PostMapping(path = "/add/{item_id}")
    public ResponseEntity<String> addToCart(Authentication authentication, @PathVariable long item_id, @RequestParam int qtyNeeded){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Wholesaler);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        Item item = itemService.getItemById(item_id);

        if (item == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such item exists!");

        if (item.getSoldBy().getRole() == Role.Wholesaler && user.getRole() == Role.Customer)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Customers cannot directly interact with wholesalers!");

        if (item.getAvailabilityStatus() == AvailabilityStatus.UNAVAILABLE)
            return new ResponseEntity<>("Item is currently unavailable", HttpStatus.OK);

        if (qtyNeeded > item.getQuantityAvailable() || qtyNeeded <= 0)
            return new ResponseEntity<>("Quantity requested for exceeds availability", HttpStatus.OK);

        List<CartItem> cartItems = user.getCart();
        boolean exists = false;
        for (CartItem cartItem : cartItems){
            if (cartItem.getItem().getId().equals(item.getId())){
                if (cartItem.getQtyInCart() + qtyNeeded > item.getQuantityAvailable()){
                    return new ResponseEntity<>("Quantity requested for exceeds availability", HttpStatus.OK);
                }
                cartItem.setQtyInCart(cartItem.getQtyInCart() + qtyNeeded);
                exists = true;
                break;
            }
        }

        if (!exists)
            cartItems.add(new CartItem(user, item, qtyNeeded));

        userService.updateUser(user);

        return new ResponseEntity<>("Added to cart!", HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @PostMapping("/remove/{item_id}")
    public ResponseEntity<String> removeFromCart(Authentication authentication, @PathVariable long item_id, @RequestParam int removeQty){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Wholesaler);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        if (removeQty < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a negative value");

        Item item = itemService.getItemById(item_id);

        if (item == null)
            throw new ResponseStatusException( HttpStatus.NOT_FOUND, "Item not in cart");

        List<CartItem> cart = user.getCart();
        CartItem itemToRemove = cartService.findByItemIdAndUser(item, user);

        if (itemToRemove == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart");

        if (itemToRemove.getQtyInCart() - removeQty > 0)
            itemToRemove.setQtyInCart(itemToRemove.getQtyInCart() - removeQty);
        else
            cart.remove(itemToRemove);

        userService.updateUser(user);
        return new ResponseEntity<>("Removed from cart!", HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @PostMapping("/checkout")
    // Add a way to verify that the payment has been completed
    public ResponseEntity<String> processCartCheckout(Authentication authentication, @RequestBody CheckoutInfo checkoutInfo){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Wholesaler);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        List<CartItem> userCart = user.getCart();
        if (userCart.isEmpty()){
            return new ResponseEntity<>("Cannot checkout an empty cart", HttpStatus.OK);
        }

        // Validate cart before proceeding
        Pair<Boolean, String> cartVerify = cartService.verifyCart(userCart);
        if (!cartVerify.getValue0())
            return new ResponseEntity<>(cartVerify.getValue1(), HttpStatus.OK);

        Purchase purchase = new Purchase();
        purchase.setInitiatedBy(user);
        purchaseService.save(purchase);

        List<Order> ordersInPurchase = new ArrayList<>();

        double total = 0.0;
        for (CartItem cartItem : userCart){
            Order order = new Order();
            order.setSoldBy(cartItem.getItem().getSoldBy());
            order.setBoughtBy(cartItem.getUser());
            order.setOrderStatus(OrderStatus.PENDING);
            order.setOrderedItem(cartItem.getItem());
            order.setOrderQuantity(cartItem.getQtyInCart());
            order.setUnits(cartItem.getItem().getQtyUnits());
//            order.setDeliveryAddress(checkoutInfo.getAddress());
            order.setDeliveryMode(order.getOrderedItem().getDeliveryMode());
            if (checkoutInfo.getAddress().isEmpty())
                order.setDeliveryAddress(user.getAddress());
            else
                order.setDeliveryAddress(checkoutInfo.getAddress());

            order.setPurchaseInvolvingOrder(purchase);
            total += cartItem.getItem().getPrice() * cartItem.getQtyInCart();

            cartItem.getItem().setQuantityAvailable(cartItem.getItem().getQuantityAvailable() - cartItem.getQtyInCart()); // Decrease cart quantity, if seller rejects the order then release it back
           // itemService.save(cartItem.getItem());

            ordersInPurchase.add(order);
            orderService.save(order);
        }

        purchase.setOrdersInPurchase(ordersInPurchase);
        purchase.setTotalPrice(total);
        userCart.clear();
        user.getPurchaseHistory().add(purchase);

        userService.updateUser(user);
        return new ResponseEntity<>("Created orders!", HttpStatus.OK);
    }
}
