package com.example.aws_project01.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductEvent {
    private Long productId;
    private String code;
    private String username;
}
