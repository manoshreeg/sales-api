package com.example.sales;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/** Rule engine: first matching rule (by priority, then id) decides the category. */
@Service
public class Categorizer {
    public static final String UNCATEGORIZED = "Uncategorized";
    private final CategoryRuleRepository repo;

    public Categorizer(CategoryRuleRepository repo) { this.repo = repo; }

    public List<CategoryRule> activeRules() { return repo.findByActiveTrueOrderByPriorityAscIdAsc(); }

    public Optional<CategoryRule> match(String product, List<CategoryRule> rules) {
        String name = product == null ? "" : product.toLowerCase();
        return rules.stream().filter(r -> name.contains(r.keyword.toLowerCase())).findFirst();
    }

    public String categorize(String product, List<CategoryRule> rules) {
        return match(product, rules).map(r -> r.category).orElse(UNCATEGORIZED);
    }
}
