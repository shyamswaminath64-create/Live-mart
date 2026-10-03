package com.example.livemart.controller;

import com.example.livemart.dto.ItemDTO;
import com.example.livemart.dto.OrderDTO;
import com.example.livemart.dto.PurchaseDTO;
import com.example.livemart.dto.ReviewDTO;
import com.example.livemart.model.*;
import com.example.livemart.model.creators.ItemCreator;
import com.example.livemart.model.creators.OrderUpdator;
import com.example.livemart.model.enums.OrderStatus;
import com.example.livemart.model.enums.Role;
import com.example.livemart.service.ItemReviewService;
import com.example.livemart.service.ItemService;
import com.example.livemart.service.OrderService;
import com.example.livemart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.javatuples.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping(path = "/user")
public class UserController {
    private final UserService userService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final ItemReviewService itemReviewService;

    public UserController(UserService userService, ItemService itemService, OrderService orderService, ItemReviewService itemReviewService){
        this.userService = userService;
        this.itemService = itemService;
        this.orderService = orderService;
        this.itemReviewService = itemReviewService;
    }

    // Manage items
    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @PostMapping(path = "/create_item")
    public ResponseEntity<String> createItem(Authentication authentication, @RequestBody ItemCreator itemCreator){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Customer);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        if (!ItemService.validateItemCreator(itemCreator))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid item creation entity");

        Item item = ItemCreator.mapToItem(itemCreator);
        item.setSoldBy(user);

        boolean saved = itemService.save(item);
        if (saved)
            return new ResponseEntity<>("Saved", HttpStatus.OK);
        return new ResponseEntity<>("Could not save", HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @PostMapping(path = "/delete_item/{item_id}")
    public ResponseEntity<String> deleteItem(Authentication authentication, @PathVariable long item_id){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Customer);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        Item itemToRemove = itemService.getItemByIdOwnedByUser(item_id, user);
        if (itemToRemove == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such item exists that is owned by you!");

        boolean deleted = itemService.delete(itemToRemove.getId());

        if (deleted)
            return new ResponseEntity<>("Successfully removed!", HttpStatus.OK);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR ,"Could not remove");
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @PostMapping(path = "/update_item/{item_id}")
    public ResponseEntity<String> updateItem(Authentication authentication, @PathVariable long item_id, @RequestBody ItemCreator itemCreator){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Customer);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        Item itemToUpdate = itemService.getItemByIdOwnedByUser(item_id, user);

        // Maybe add a check to see if any orders reference this item before updating it

        if (itemToUpdate == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such item exists that is owned by you!");

        if (!ItemService.validateItemCreator(itemCreator))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid item creation entity");

        ItemCreator.mapToExistingItem(itemToUpdate, itemCreator);

        boolean updated = itemService.save(itemToUpdate); // update
        if (updated)
            return new ResponseEntity<>("Successfully updated!", HttpStatus.OK);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR ,"Could not update");
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @PostMapping(path = "/create_review")
    public ResponseEntity<String> createReview(Authentication authentication, ReviewDTO reviewDTO){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Wholesaler);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        Item item = itemService.getItemById(reviewDTO.getItemID());

        if (item == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot review this item");

        boolean wasBought = false;
        for (Order order : orderService.getAllOrdersBoughtByUser(user)){
            if (order.getOrderedItem().getId().longValue() == item.getId().longValue() && order.getOrderStatus() == OrderStatus.DELIVERED){
                wasBought = true;
                break;
            }
        }

        if (!wasBought)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot review this item");

        ItemReview itemReview = new ItemReview();
        itemReview.setItem(item);
        itemReview.setReview(reviewDTO.getReview());
        itemReview.setRatingGiven(reviewDTO.getRating());

        item.getReviewsForItem().add(itemReview);
        item.setRating((item.getRating() + reviewDTO.getRating())/item.getReviewsForItem().size());

        itemService.save(item);

        return new ResponseEntity<>("Rating given", HttpStatus.OK);
    }

    // Manage orders
    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/incoming_orders")
    public ResponseEntity<List<OrderDTO>> getIncomingOrders(Authentication authentication, @RequestParam OrderStatus orderStatus){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Customer);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Order order : user.getOrders()) {
            if (order.getOrderStatus() == orderStatus)
                orderDTOList.add(OrderDTO.createDTOFromOrder(order));
        }

        return new ResponseEntity<>(orderDTOList, HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @PostMapping(path = "/update_order/{order_id}")
    public ResponseEntity<String> updateOrder(Authentication authentication, @PathVariable long order_id, @RequestBody OrderUpdator orderUpdator){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Customer);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        Optional<Order> order = orderService.getOrderByIdAndSoldByUser(order_id, user);
        if (order.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such order exists that belongs to you");

        order.get().setOrderStatus(orderUpdator.getOrderStatus());

        try{
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH);
            order.get().setExpectedDeliveryDate(LocalDate.parse(orderUpdator.getExpectedDeliveryDate(), dateTimeFormatter));
            orderService.update(order.get());
            return new ResponseEntity<>("Successfully updated order!", HttpStatus.OK);
        }
        catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date/time format");
        }
    }


    // Some views
    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/purchase_history")
    public ResponseEntity<List<PurchaseDTO>> getPurchaseHistory(Authentication authentication){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Wholesaler);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        List<PurchaseDTO> purchaseDTO = new ArrayList<>();

        for (Purchase purchase : user.getPurchaseHistory()) {
            purchaseDTO.add(PurchaseDTO.createDTOFromPurchase(purchase));
        }

        return new ResponseEntity<>(purchaseDTO, HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/placed_orders")
    public ResponseEntity<List<OrderDTO>> getPlacedOrders(Authentication authentication){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Wholesaler);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        List<OrderDTO> orderDTOS = new ArrayList<>();
        for (Order order : orderService.getAllOrdersBoughtByUser(user)){
            orderDTOS.add(OrderDTO.createDTOFromOrder(order));
        }

        return new ResponseEntity<>(orderDTOS, HttpStatus.OK);
    }


    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/my_items")
    public ResponseEntity<List<ItemDTO>> getItemsBeingSold(Authentication authentication){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, Role.Customer);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        return new ResponseEntity<>(user.getItemsCurrentlyBeingSold().stream()
                .map(ItemDTO::mapItemToDTO)
                .toList(), HttpStatus.OK);
    }
}
