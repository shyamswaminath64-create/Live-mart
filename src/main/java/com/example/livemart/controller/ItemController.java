package com.example.livemart.controller;

import com.example.livemart.dto.ItemDTO;
import com.example.livemart.model.Item;
import com.example.livemart.model.User;
import com.example.livemart.model.enums.Role;
import com.example.livemart.service.ItemService;
import com.example.livemart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/item")
public class ItemController {
    private UserService userService;
    private ItemService itemService;

    public ItemController(UserService userService, ItemService itemService){
        this.userService = userService;
        this.itemService = itemService;
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/{item_id}")
    public ResponseEntity<ItemDTO> viewItem(@PathVariable long item_id){
        Item item = itemService.getItemById(item_id);

        if (item == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item requested for does not exist");

        return new ResponseEntity<>(ItemDTO.mapItemToDTO(item), HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/category")
    public ResponseEntity<List<ItemDTO>> getByCategory(@RequestParam String category){
        List<Item> byCategory = itemService.findByCategory(category);
        List<ItemDTO> byCategoryDTO = new ArrayList<>();
        for (Item item : byCategory){
            byCategoryDTO.add(ItemDTO.mapItemToDTO(item));
        }
        return new ResponseEntity<>(byCategoryDTO, HttpStatus.OK);
    }

    @Operation(security = {@SecurityRequirement(name = "bearer-jwt"), @SecurityRequirement(name = "oauth2")})
    @GetMapping(path = "/search")
    public ResponseEntity<List<ItemDTO>> getSimilarItems(@RequestParam String query){
        List<Item> similarItems = itemService.searchSimilar(query);
        List<ItemDTO> similarItemDTO = new ArrayList<>();
        for (Item item : similarItems){
            similarItemDTO.add(ItemDTO.mapItemToDTO(item));
        }

        return new ResponseEntity<>(similarItemDTO, HttpStatus.OK);
    }
}
