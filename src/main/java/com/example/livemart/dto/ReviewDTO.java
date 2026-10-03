package com.example.livemart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewDTO {
    private Long itemID;

    private String review;
    private Integer rating;
}
