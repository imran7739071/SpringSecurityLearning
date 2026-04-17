package com.imri.spring_security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder          // lets us do ProductResponse.builder().name("x").build()
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private String category;
    private String createdBy;    // just the username, not the whole User object
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}