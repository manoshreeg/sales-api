package com.example.sales;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RuleController {
    private final CategoryRuleRepository repo;
    private final Categorizer categorizer;

    public RuleController(CategoryRuleRepository repo, Categorizer categorizer) {
        this.repo = repo; this.categorizer = categorizer;
    }

    @GetMapping
    public List<CategoryRule> all() { return repo.findAll(Sort.by("priority", "id")); }

    @PostMapping // ADMIN only
    public CategoryRule create(@Valid @RequestBody CategoryRule rule) {
        rule.id = null;
        rule.keyword = rule.keyword.trim();
        return repo.save(rule);
    }

    @DeleteMapping("/{id}") // ADMIN only
    public void delete(@PathVariable Long id) { repo.deleteById(id); }

    /** Dry run: shows which rule would fire for a product name. */
    @GetMapping("/test")
    public Map<String, Object> test(@RequestParam String product) {
        var m = categorizer.match(product, categorizer.activeRules());
        return Map.of("product", product,
                "category", m.map(r -> r.category).orElse(Categorizer.UNCATEGORIZED),
                "matchedRule", m.map(r -> r.keyword + " (priority " + r.priority + ")").orElse("none"));
    }
}
