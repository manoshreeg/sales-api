package com.example.sales;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(indexes = {@Index(columnList = "region"), @Index(columnList = "productName"), @Index(columnList = "saleDate"), @Index(columnList = "category")})
public class Sale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String productName;
    public String region;
    public int quantity;
    public BigDecimal unitPrice;
    public LocalDate saleDate;
    public String category;
}
