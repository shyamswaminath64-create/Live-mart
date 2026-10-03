package com.example.livemart.controller;

import com.example.livemart.common.exceptions.UserNotFoundException;
import com.example.livemart.dto.UserHomeDTO;
import com.example.livemart.model.Item;
import com.example.livemart.model.User;
import com.example.livemart.model.enums.Role;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/store")
public class StoreController {
    private final UserService userService;
    private final ItemService itemService;
    private final OrderService orderService;

    public StoreController(UserService userService, ItemService itemService, OrderService orderService){
        this.userService = userService;
        this.itemService = itemService;
        this.orderService = orderService;
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/{user_id}")
    public ResponseEntity<UserHomeDTO> getPublicHomePage(@PathVariable long user_id){
        User userQueried;
        try{
            userQueried = userService.findUserById(user_id);
            if (userQueried.getAuthorized() == false)
                throw new UserNotFoundException("Not authorized");
        }
        catch (UserNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return new ResponseEntity<>(UserHomeDTO.mapFromUser(userQueried), HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/search")
    public ResponseEntity<List<UserHomeDTO>> searchStores(@RequestParam String query){
        List<User> similarStores = userService.searchSimilar(query);
        List<UserHomeDTO> similarStoreDTO = new ArrayList<>();
        for (User user : similarStores){
            if (user.getRole() != Role.Customer)
                similarStoreDTO.add(UserHomeDTO.mapFromUser(user));
        }

        return new ResponseEntity<>(similarStoreDTO, HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/nearby")
    public ResponseEntity<List<UserHomeDTO>> nearbyStores(Authentication authentication, @RequestParam Integer maxDistance){
        User user = userService.extractUserFromAuthentication(authentication);

        Pair<Boolean, String> validation = userService.validateAccess(user, null);
        if (!validation.getValue0()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, validation.getValue1());
        }

        List<User> nearbyUsers = userService.findUsersWithinDistance(user.getLatitude(), user.getLongitude(), maxDistance);
        List<UserHomeDTO> userHomeDTOS = new ArrayList<>();

        for (User user_ : nearbyUsers){
            if (user_.getId().longValue() != user.getId().longValue())
                userHomeDTOS.add(UserHomeDTO.mapFromUser(user_));
        }

        return new ResponseEntity<>(userHomeDTOS, HttpStatus.OK);
    }
}
