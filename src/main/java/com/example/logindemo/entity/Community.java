package com.example.logindemo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Community {
    private Long id;
    private String name;
    private String city;
    private String district;
    private String address;
    private BigDecimal price;
    private BigDecimal rating;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}