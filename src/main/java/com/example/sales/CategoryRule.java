package com.example.sales;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/** A keyword rule: if the product name contains `keyword`, assign `category`. Lower priority number wins. */
@Entity
public class CategoryRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @NotBlank public String keyword;
    @NotBlank public String category;
    public int priority = 100;
    public boolean active = true;

    public CategoryRule() {}
    public CategoryRule(String keyword, String category, int priority) {
        this.keyword = keyword; this.category = category; this.priority = priority;
    }
}
