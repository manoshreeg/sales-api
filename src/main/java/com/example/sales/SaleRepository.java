package com.example.sales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    interface Total { String getLabel(); BigDecimal getTotal(); }

    Page<Sale> findByRegionContainingIgnoreCase(String region, Pageable pageable);

    @Query("select s.region as label, sum(s.quantity * s.unitPrice) as total from Sale s group by s.region")
    List<Total> revenueByRegion();

    @Query("select s.productName as label, sum(s.quantity * s.unitPrice) as total from Sale s " +
           "group by s.productName order by sum(s.quantity * s.unitPrice) desc")
    List<Total> topProducts(Pageable pageable);

    @Query("select concat(year(s.saleDate), '-', month(s.saleDate)) as label, sum(s.quantity * s.unitPrice) as total " +
           "from Sale s group by year(s.saleDate), month(s.saleDate) order by year(s.saleDate), month(s.saleDate)")
    List<Total> revenueByMonth();

    @Query("select s.category as label, sum(s.quantity * s.unitPrice) as total from Sale s group by s.category")
    List<Total> revenueByCategory();

    long countByCategory(String category);

    @Query("select distinct s.productName from Sale s where s.category = 'Uncategorized' or s.category is null")
    List<String> uncategorizedProducts();
}
