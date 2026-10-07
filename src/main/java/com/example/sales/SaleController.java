package com.example.sales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleRepository repo;
    private final Categorizer categorizer;

    public SaleController(SaleRepository repo, Categorizer categorizer) {
        this.repo = repo; this.categorizer = categorizer;
    }

    /** CSV columns: productName,region,quantity,unitPrice,saleDate(yyyy-MM-dd). Bad rows are rejected and reported. */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        List<Sale> valid = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<CategoryRule> rules = categorizer.activeRules();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            r.readLine(); // header
            int row = 1;
            String line;
            while ((line = r.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                try {
                    String[] p = line.split(",", -1);
                    if (p.length != 5) throw new IllegalArgumentException("expected 5 columns");
                    Sale s = new Sale();
                    s.productName = p[0].trim();
                    s.region = p[1].trim();
                    if (s.productName.isEmpty() || s.region.isEmpty())
                        throw new IllegalArgumentException("product and region are required");
                    s.quantity = Integer.parseInt(p[2].trim());
                    s.unitPrice = new BigDecimal(p[3].trim());
                    s.saleDate = LocalDate.parse(p[4].trim());
                    s.category = categorizer.categorize(s.productName, rules);
                    if (s.quantity <= 0 || s.unitPrice.signum() < 0)
                        throw new IllegalArgumentException("quantity must be > 0 and price >= 0");
                    valid.add(s);
                } catch (Exception e) {
                    errors.add("Row " + row + ": " + e.getMessage());
                }
            }
        }
        repo.saveAll(valid);
        long uncategorized = valid.stream().filter(x -> Categorizer.UNCATEGORIZED.equals(x.category)).count();
        return Map.of("saved", valid.size(), "rejected", errors.size(), "uncategorized", uncategorized, "errors", errors);
    }

    @GetMapping
    public Page<Sale> list(@RequestParam(defaultValue = "") String region, Pageable pageable) {
        return repo.findByRegionContainingIgnoreCase(region, pageable);
    }

    @GetMapping("/reports/by-region")
    public List<SaleRepository.Total> byRegion() { return repo.revenueByRegion(); }

    @GetMapping("/reports/by-month")
    public List<SaleRepository.Total> byMonth() { return repo.revenueByMonth(); }

    @GetMapping("/reports/top-products")
    public List<SaleRepository.Total> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return repo.topProducts(PageRequest.of(0, Math.min(Math.max(limit, 1), 50)));
    }

    @GetMapping("/reports/by-category")
    public List<SaleRepository.Total> byCategory() { return repo.revenueByCategory(); }

    /** Data-quality metric: how much of the data the rules managed to classify. */
    @GetMapping("/reports/category-coverage")
    public Map<String, Object> coverage() {
        long total = repo.count();
        long unc = repo.countByCategory(Categorizer.UNCATEGORIZED);
        double pct = total == 0 ? 0 : Math.round((total - unc) * 10000.0 / total) / 100.0;
        return Map.of("totalSales", total, "uncategorized", unc, "categorizedPercent", pct);
    }

    /** Review queue: distinct product names no rule matched. */
    @GetMapping("/uncategorized")
    public List<String> uncategorized() { return repo.uncategorizedProducts(); }

    /** Re-run all active rules over existing sales (ADMIN), e.g. after adding a new rule. */
    @PostMapping("/recategorize")
    public Map<String, Object> recategorize() {
        List<CategoryRule> rules = categorizer.activeRules();
        List<Sale> all = repo.findAll();
        int updated = 0;
        for (Sale s : all) {
            String c = categorizer.categorize(s.productName, rules);
            if (!c.equals(s.category)) { s.category = c; updated++; }
        }
        repo.saveAll(all);
        return Map.of("updated", updated);
    }
}
